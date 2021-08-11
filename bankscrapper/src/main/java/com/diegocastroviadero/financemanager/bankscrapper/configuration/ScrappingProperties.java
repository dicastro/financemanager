package com.diegocastroviadero.financemanager.bankscrapper.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;
import java.nio.file.Path;

import static com.diegocastroviadero.financemanager.bankscrapper.configuration.ScrappingProperties.SCRAPPING_CONFIG_PREFIX;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = SCRAPPING_CONFIG_PREFIX)
public class ScrappingProperties {
    public static final String SCRAPPING_CONFIG_PREFIX = "bankscrapper.scrapping";

    private SeleniumHubProperties seleniumHub;
    private TesseractProperties tesseract;
    private BanksProperties banks;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SeleniumHubProperties {
        private URL url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TesseractProperties {
        private Path commandPath;
        private Path basePath;
        private Path dataPath;
        private Path configPath;
        private String lang;
        private Integer pageSegmentationMode;
        private Integer ocrEngineMode;
        private Integer dpi;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BanksProperties {
        private KbProperties kb;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class KbProperties {
        private KeyboardCacheProperties keyboardCache;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class KeyboardCacheProperties {
        private Path basePath;
        private Path cache;
        private Path tmp;
    }
}
