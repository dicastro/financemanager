package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.service;

import com.diegocastroviadero.financemanager.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.financemanager.bankscrapper.model.Account;
import com.diegocastroviadero.financemanager.bankscrapper.model.Bank;
import com.diegocastroviadero.financemanager.bankscrapper.model.Movement;
import com.diegocastroviadero.financemanager.bankscrapper.model.SyncType;
import com.diegocastroviadero.financemanager.bankscrapper.persistence.service.AccountInventoryService;
import com.diegocastroviadero.financemanager.bankscrapper.persistence.service.AccountSyncLogService;
import com.diegocastroviadero.financemanager.bankscrapper.persistence.service.MovementsService;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.DateFilter;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.model.SecurityContext;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.common.utils.ScrapperUtils;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.console.KbBankCredential;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.keyboard.Keyboard;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.keyboard.KeyboardManager;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.keyboard.UnparseableKeyboardException;
import com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.utils.KbUtils;
import com.diegocastroviadero.financemanager.bankscrapper.utils.Utils;
import com.diegocastroviadero.financemanager.cryptoutils.exception.CsvIOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ScrapperKbService {
    private final Integer ONE_SECOND = 1000;
    private final Integer FIVE_SECONDS = 5000;

    private final ScrappingProperties properties;
    private final KeyboardManager keyboardManager;

    private final AccountInventoryService accountInventoryService;
    private final AccountSyncLogService accountSyncLogService;
    private final MovementsService movementsService;

    public void scrap(final SyncType syncType, final Boolean whatIfMode) {
        final DateFilter dateFilter = ScrapperUtils.newDateFilterFrom(syncType);

        log.info("Scrapping data from Kutxabank from {} to {} ...", dateFilter.getFrom(), dateFilter.getTo());

        if (whatIfMode) {
            log.info("WhatIf mode was activated: scrapping is not done");
        } else {
            RemoteWebDriver driver = null;

            try {
                driver = getDriver();

                login(driver, SecurityContext.getBankCredentials().get(getBank()).castTo(KbBankCredential.class));

                final boolean switchedToTab = swithToTab(driver);

                if (switchedToTab) {
                    goToStart(driver);

                    final List<String> rawAccounts = getUserAccounts(driver);

                    final List<Account> accounts = accountInventoryService.registerAccounts(getBank(), rawAccounts);

                    final ZonedDateTime syncTimestamp = Utils.now();

                    for (final Account account : accounts) {
                        accountSyncLogService.markAccountAsSyncing(getBank(), account.getId(), dateFilter.getMonthsGroupedByYear(), syncTimestamp);

                        goToStart(driver);

                        final List<Movement> accountMovements = getAccountMovements(driver, account, dateFilter);

                        final Map<YearMonth, List<Movement>> accountMovementsByYearMonth = movementsService.groupAccountMovementsByYearMonth(accountMovements);

                        movementsService.persistMovements(account.getId(), accountMovementsByYearMonth);

                        accountSyncLogService.markAccountAsSynced(getBank(), account.getId(), dateFilter.getMonthsGroupedByYear(), syncTimestamp);
                    }
                }
            } catch (Exception e) {
                log.error("Error while scrapping data from Kutxabank", e);

                try {
                    accountSyncLogService.markBankSyncingAccountsAsSyncError(getBank(), dateFilter.getMonthsGroupedByYear(), e.getMessage());
                } catch (CsvIOException csvioe) {
                    log.error("Error while marking as error kutxabank accounts that were syncing", csvioe);
                }
            } finally {
                if (driver != null) {
                    driver.quit();
                }
            }
        }
    }

    private Bank getBank() {
        return Bank.KB;
    }

    private RemoteWebDriver getDriver() {
        final FirefoxProfile profile = new FirefoxProfile();

        final FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
        options.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);

        final RemoteWebDriver driver = new RemoteWebDriver(properties.getSeleniumHub().getUrl(), options);

        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();

        return driver;
    }

    private void login(final RemoteWebDriver driver, final KbBankCredential bankCredential) throws IOException, UnparseableKeyboardException {
        driver.navigate().to("https://portal.kutxabank.es/cs/Satellite/kb/es/particulares");

        driver.findElementsByClassName("cookies-boton").stream()
                .filter(WebElement::isDisplayed)
                .filter(webElement -> webElement.getText().equalsIgnoreCase("Aceptar todas"))
                .findFirst()
                .ifPresent(WebElement::click);

        final WebElement user = driver.findElementById("usuario");
        user.sendKeys(bankCredential.getUsername());

        final WebElement password = driver.findElementById("password_PAS");
        password.click();

        waitMillis(FIVE_SECONDS);

        final WebDriverWait wait = new WebDriverWait(driver, 10);
        final WebElement keyboardImg = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tecladoImg")));

        final Point keyboardImgLocation = keyboardImg.getLocation();
        final Dimension keyboardImgSize = keyboardImg.getSize();

        final Path fullScreenshotPath = properties.getBanks().getKb().getKeyboardCache().getTmp().resolve("full_screenshot.png");

        final byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(fullScreenshotPath, screenshotBytes, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        final BufferedImage screenshotBufferedImage = ImageIO.read(fullScreenshotPath.toFile());
        final BufferedImage screenshotSection = screenshotBufferedImage.getSubimage(keyboardImgLocation.getX() + 1, keyboardImgLocation.getY(), keyboardImgSize.getWidth(), keyboardImgSize.getHeight());

        final Path keyboardScreenshotPath = properties.getBanks().getKb().getKeyboardCache().getTmp().resolve("keyboard_screenshot.png");
        ImageIO.write(screenshotSection, "png", keyboardScreenshotPath.toFile());

        fullScreenshotPath.toFile().deleteOnExit();

        final Keyboard keyboard = keyboardManager.parseKeyboard(keyboardScreenshotPath.toFile());

        final Actions actions = new Actions(driver);
        keyboard.getSequenceOfPassword(bankCredential.getPassword())
                .forEach(offset -> {
                    actions
                            .moveToElement(keyboardImg)
                            .moveByOffset(-(keyboardImgSize.getWidth() / 2), -(keyboardImgSize.getHeight() / 2))
                            .moveByOffset(offset.getX(), offset.getY())
                            .click().perform();
                    waitMillis(500);
                });

        log.debug("Pasword introduced !");

        waitMillis(ONE_SECOND);

        driver.findElementById("enviar").click();

        waitMillis(ONE_SECOND);
    }

    private void goToStart(final RemoteWebDriver driver) {
        final WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(("//div[@id = 'formMenuSuperior:PanelSuperior']//a[text() = 'Resumen']"))))
                .click();
    }

    private List<String> getUserAccounts(final RemoteWebDriver driver) {
        return driver.findElementsByXPath("//div[contains(@class, 'posiciones_tituloSeccion')]/span[text() = 'Cuentas']/../..//tr[contains(@class, 'posicicones_tablaListaContratosRow')]/td[contains(@class, 'posiciones_columna1Posicion')]").stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private List<Movement> getAccountMovements(final RemoteWebDriver driver, final Account account, final DateFilter dateFilter) {
        final WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(String.format("//span[@class='iceOutTxt' and text()='%s']/../..//span[text()='Movimientos']/..", account.getAccountNumber()))))
                .click();

        waitMillis(ONE_SECOND);

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//label[text()='Entre fechas']/../input")))
                .click();

        waitMillis(ONE_SECOND);

        boolean allFilterCriteriaFieldsHaveValue;

        WebElement dateField = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='formCriterios:calendarioDesde_cmb_dias']")));
        allFilterCriteriaFieldsHaveValue = ensureFieldHasValue(driver, dateField, "formCriterios:calendarioDesde_cmb_dias", dateFilter.getFromD());

        dateField = driver.findElementByXPath("//input[@id='formCriterios:calendarioDesde_cmb_mes']");
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioDesde_cmb_mes", dateFilter.getFromM());

        dateField = driver.findElementByXPath("//input[@id='formCriterios:calendarioDesde_cmb_anyo']");
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioDesde_cmb_anyo", dateFilter.getFromY());

        dateField = driver.findElementByXPath("//input[@id='formCriterios:calendarioHasta_cmb_dias']");
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioHasta_cmb_dias", dateFilter.getToD());

        dateField = driver.findElementByXPath("//input[@id='formCriterios:calendarioHasta_cmb_mes']");
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioHasta_cmb_mes", dateFilter.getToM());

        dateField = driver.findElementByXPath("//input[@id='formCriterios:calendarioHasta_cmb_anyo']");
        allFilterCriteriaFieldsHaveValue &= ensureFieldHasValue(driver, dateField, "formCriterios:calendarioHasta_cmb_anyo", dateFilter.getToY());

        if (!allFilterCriteriaFieldsHaveValue) {
            throw new RuntimeException("Error while setting values of fields to filter account movements by date");
        }

        driver.findElementById("formCriterios:mostrar").click();

        log.debug("Getting account movements with date filter {} ...", dateFilter);

        final List<Movement> pageMovements = getPageMovements(wait, account);

        while (moreMovementPages(driver)) {
            nextMovementsPage(driver);

            pageMovements.addAll(getPageMovements(wait, account));
        }

        return pageMovements;
    }

    private void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    private boolean swithToTab(final RemoteWebDriver driver) {
        boolean switchedToTab = false;

        final Set<String> tabs = driver.getWindowHandles();
        final Iterator<String> tabsIterator = tabs.iterator();

        log.debug("There are {} tabs in the browser, looking for the right one", tabs.size());

        while (tabsIterator.hasNext() && !switchedToTab) {
            final String tab = tabsIterator.next();

            driver.switchTo().window(tab);

            final String currentTabTitle = driver.getTitle();

            final String TAB_TITLE = "Kutxabank";
            switchedToTab = StringUtils.equals(TAB_TITLE, currentTabTitle);

            if (!switchedToTab) {
                log.debug("Current tab (title: '{}') is not the right one (expected title: '{}'), switching to the next one ...", currentTabTitle, TAB_TITLE);
                waitMillis(2000);
            } else {
                log.debug("Tab found!");
                driver.manage().window().maximize();
            }
        }

        if (!switchedToTab) {
            log.warn("Expected tab could not be found between all the browser tabs");
        }

        return switchedToTab;
    }

    private void clickAndSendKeys(final WebElement element, final String text) {
        element.click();
        for (String character : text.split("")) {
            waitMillis(100);
            element.sendKeys(character);
        }
    }

    private List<Movement> getPageMovements(final WebDriverWait wait, final Account account) {
        // TODO: sometimes table is not shown because of a page error, in this case movements recovery should be restarted
        final WebElement movementsTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@id='formListado:dataContent']")));

        final List<WebElement> movementsRows = movementsTable.findElements(By.xpath(".//table"));

        return movementsRows.stream()
                .map(movementRowTable -> {
                    final List<WebElement> movementColumns = movementRowTable.findElements(By.xpath(".//td"));

                    final LocalDate parsedDate = KbUtils.parseKbMovementDate(movementColumns.get(0).getText());
                    final String concept = movementColumns.get(1).getText();
                    final LocalDate parsedDateValue = KbUtils.parseKbMovementDate(movementColumns.get(2).getText());
                    final Long parsedQuantity = KbUtils.parseKbMovementQuantity(movementColumns.get(3).getText());

                    return Movement.builder()
                            .bank(getBank())
                            .accountId(account.getId())
                            .account(account.getAccountNumber())
                            .date(parsedDate)
                            .concept(concept)
                            .dateValue(parsedDateValue)
                            .quantity(parsedQuantity)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private boolean moreMovementPages(final RemoteWebDriver driver) {
        return driver.findElementsById("formListado:siguiente").stream()
                .anyMatch(WebElement::isDisplayed);
    }

    private void nextMovementsPage(final RemoteWebDriver driver) {
        driver.findElementById("formListado:siguiente").click();
        waitMillis(FIVE_SECONDS);
    }

    private boolean ensureFieldHasValue(final RemoteWebDriver driver, final WebElement element, final String elementId, final String value) {
        boolean fieldHasValue;
        int times = 0;

        String currentValue = (String) driver.executeScript(String.format("return document.getElementById('%s').value", elementId));

        while (!(fieldHasValue = StringUtils.equals(currentValue, value)) && times < 10) {
            if (times > 0) {
                waitMillis(500);
            }
            clickAndSendKeys(element, value);
            currentValue = (String) driver.executeScript(String.format("return document.getElementById('%s').value", elementId));
            times++;
        }

        return fieldHasValue;
    }
}
