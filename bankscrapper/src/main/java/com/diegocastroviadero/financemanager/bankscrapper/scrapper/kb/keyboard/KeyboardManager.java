package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.keyboard;

import com.diegocastroviadero.financemanager.bankscrapper.configuration.ScrappingProperties;
import com.diegocastroviadero.financemanager.bankscrapper.configuration.ScrappingProperties.TesseractProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class KeyboardManager {

    private final ScrappingProperties properties;

    private final Map<String, Integer> digitHashesCache = new HashMap<>();

    public KeyboardManager(final ScrappingProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    private void postConstruct() {
        log.info("Loading cached keyboard keys ...");

        Stream.of(Objects.requireNonNull(properties.getBanks().getKb().getKeyboardCache().getCache().toFile().listFiles()))
                .filter(File::isFile)
                .forEach(keyboardImageFile -> {
                    // digit images will be stored with file name pattern: <digit>_<sha1Hash>.png
                    final String fileName = FilenameUtils.getBaseName(keyboardImageFile.getName());
                    final String[] fileNameParts = fileName.split("_");

                    digitHashesCache.put(fileNameParts[1], Integer.valueOf(fileNameParts[0]));
                });

        log.info("Loaded {} cached keyboard keys !", digitHashesCache.size());

        log.info("Cleaning temp keyboard cache ...");

        Stream.of(Objects.requireNonNull(properties.getBanks().getKb().getKeyboardCache().getTmp().toFile().listFiles()))
                .filter(File::isFile)
                .forEach(File::delete);

        log.info("Cleaned temp keyboard cache !");
    }

    public Keyboard parseKeyboard(final File keyboardImageFile) throws UnparseableKeyboardException {
        final List<CandidateKey> keyboardKeys = splitKeyboardImage(keyboardImageFile);

        final Map<Integer, Offset> keyboardDigits = new HashMap<>();

        for (final CandidateKey keyboardKey : keyboardKeys) {
            final Integer digit;

            if (digitHashesCache.containsKey(keyboardKey.getSha1Digest())) {
                digit = digitHashesCache.get(keyboardKey.getSha1Digest());

                keyboardKey.getImageFile().delete();
            } else {
                digit = processKeyboardImagesNatively(keyboardKey);

                digitHashesCache.put(keyboardKey.getSha1Digest(), digit);

                keyboardKey.getImageFile().renameTo(properties.getBanks().getKb().getKeyboardCache().getCache()
                        .resolve(String.format("%d_%s.%s",
                                digit,
                                keyboardKey.getSha1Digest(),
                                FilenameUtils.getExtension(keyboardKey.getImageFile().getName())))
                        .toFile());
            }

            keyboardDigits.put(digit, keyboardKey.getCenterOffset());
        }

        return Keyboard.builder()
                .digits(keyboardDigits)
                .build();
    }

    private List<CandidateKey> splitKeyboardImage(final File keyboardImageFile) {
        List<CandidateKey> keyboardKeys;

        final String keyboardImageExtension = FilenameUtils.getExtension(keyboardImageFile.getName());

        try {
            final BufferedImage source = ImageIO.read(keyboardImageFile);

            final int keyboardWidth = source.getWidth();
            final int keyboardHeight = source.getHeight();

            final int keysInXAxis = 5;
            final int keysInYAxis = 2;

            final int keyWidth = keyboardWidth / keysInXAxis;
            final int keyHeigh = keyboardHeight / keysInYAxis;

            keyboardKeys = new ArrayList<>();

            for (int y = 0; y < keysInYAxis; y++) {
                for (int x = 0; x < keysInXAxis; x++) {
                    final int imageIndex = x + y * keysInXAxis;

                    int offsetX = x * keyWidth;
                    int offsetY = y * keyHeigh;

                    // if last key in row -> width is the rest of the image
                    int width = (x == keysInXAxis - 1) ? keyboardWidth - x * keyWidth : keyWidth;

                    // last row of keys -> height is the rest of the image
                    int height = (y == keysInYAxis - 1) ? keyboardHeight - y * keyHeigh : keyHeigh;

                    final BufferedImage keyBufferedImage = source.getSubimage(offsetX, offsetY, width, height);
                    final String sha1Digest = getSHA1Digest(keyBufferedImage);

                    final File keyImageFile = properties.getBanks().getKb().getKeyboardCache().getTmp().resolve(String.format("key_%d_%s.%s", imageIndex, sha1Digest, keyboardImageExtension)).toFile();
                    ImageIO.write(keyBufferedImage, keyboardImageExtension, keyImageFile);

                    keyboardKeys.add(CandidateKey.builder()
                            .imageFile(keyImageFile)
                            .sha1Digest(sha1Digest)
                            .centerOffset(Offset.builder()
                                    .x(offsetX + width / 2)
                                    .y(offsetY + height / 2)
                                    .build())
                            .build());
                }
            }
        } catch (IOException e) {
            keyboardKeys = Collections.emptyList();
        }

        return keyboardKeys;
    }

    private String getSHA1Digest(final BufferedImage bufferedImage) {
        final StringBuilder digestHex = new StringBuilder();

        final String digestAlgorithm = "SHA1";

        try {
            final MessageDigest md = MessageDigest.getInstance(digestAlgorithm);

            final ByteBuffer bb = ByteBuffer.allocate(4 * bufferedImage.getWidth());

            for (int y = bufferedImage.getHeight() - 1; y >= 0; y--) {
                bb.clear();

                for (int x = bufferedImage.getWidth() - 1; x >= 0; x--) {
                    bb.putInt(bufferedImage.getRGB(x, y));
                }

                md.update(bb.array());
            }

            final byte[] digest = md.digest();

            for (byte b : digest) {
                digestHex.append(String.format("%02X", b & 0xff));
            }

            return digestHex.toString();
        } catch (NoSuchAlgorithmException ignore) { // THIS SHOULD NEVER ARRIVE
            log.warn("Digest could not be calculated because digest algorithm '{}' does not exist", digestAlgorithm);
        }

        return digestHex.toString();
    }

    private int processKeyboardImagesNatively(final CandidateKey keyImage) throws UnparseableKeyboardException {
        final File tesseractCommandLocation = properties.getTesseract().getCommandPath().toFile();

        if (tesseractCommandLocation.isFile() && tesseractCommandLocation.exists() && tesseractCommandLocation.canExecute()) {
            final String outputFile = "output";

            final Path outputFilePath = properties.getBanks().getKb().getKeyboardCache().getTmp().resolve(String.format("%s.txt", outputFile));

            final TesseractProperties tessCfg = properties.getTesseract();

            final String commandLine = String.format("tesseract %s %s --tessdata-dir %s -l %s --psm %d --oem %d --dpi %d %s",
                    keyImage.getImageFile().getAbsolutePath(),
                    properties.getBanks().getKb().getKeyboardCache().getTmp().resolve(outputFile),
                    tessCfg.getDataPath().toAbsolutePath(),
                    tessCfg.getLang(),
                    tessCfg.getPageSegmentationMode(),
                    tessCfg.getOcrEngineMode(),
                    tessCfg.getDpi(),
                    tessCfg.getConfigPath().toAbsolutePath());

            final CommandLine cmdLine = CommandLine.parse(commandLine);
            final DefaultExecutor executor = new DefaultExecutor();

            try {
                final int exitValue = executor.execute(cmdLine);

                if (exitValue == 0) {
                    final String parsedKeyDigit = Files.readAllLines(outputFilePath).get(0);

                    try {
                        return Integer.parseInt(parsedKeyDigit);
                    } catch (NumberFormatException e) {
                        throw new UnparseableKeyboardException(String.format("Parsed digit by tesseract '%s' is not a number", parsedKeyDigit));
                    } finally {
                        outputFilePath.toFile().delete();
                    }
                } else {
                    throw new UnparseableKeyboardException(String.format("Unsuccessful exit value (%d) for tesseract processing image", exitValue));
                }
            } catch (IOException e) {
                throw new UnparseableKeyboardException("There was an unexpected error parsing key image with tesseract", e);
            }
        } else {
            throw new UnparseableKeyboardException("Key image could not be parsed because tesseract command is not present or is not executable");
        }
    }

    @Builder
    @Getter
    private static class CandidateKey {
        private final File imageFile;
        private final String sha1Digest;
        private final Offset centerOffset;
    }
}
