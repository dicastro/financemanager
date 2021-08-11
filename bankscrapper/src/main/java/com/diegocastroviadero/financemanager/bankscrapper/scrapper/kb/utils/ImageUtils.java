package com.diegocastroviadero.financemanager.bankscrapper.scrapper.kb.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
public class ImageUtils {
    public static void imageDifference(final String imageName1, final String imageName2) throws IOException {
        BufferedImage img1 = ImageIO.read(new File(String.format("images/%s.png", imageName1)));
        BufferedImage img2 = ImageIO.read(new File(String.format("images/%s.png", imageName2)));

        int w1 = img1.getWidth();
        int w2 = img2.getWidth();
        int h1 = img1.getHeight();
        int h2 = img2.getHeight();

        if ((w1 != w2) || (h1 != h2)) {
            log.debug("Both images ({} and {}) should have same dimwnsions", imageName1, imageName2);
        } else {
            long diff = 0;

            for (int y = 0; y < h1; y++) {
                for (int x = 0; x < w1; x++) {
                    //Getting the RGB values of a pixel
                    int pixel1 = img1.getRGB(x, y);

                    Color color1 = new Color(pixel1, true);
                    int r1 = color1.getRed();
                    int g1 = color1.getGreen();
                    int b1 = color1.getBlue();

                    int pixel2 = img2.getRGB(x, y);

                    Color color2 = new Color(pixel2, true);
                    int r2 = color2.getRed();
                    int g2 = color2.getGreen();
                    int b2 = color2.getBlue();

                    //sum of differences of RGB values of the two images
                    long data = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);

                    if (data > 0) {
                        log.warn("Differecence between images ({} and {}) in pixel at position ({}, {}). One pixel has color ({}, {}, {}) and the other one ({}, {}, {})", imageName1, imageName2, x, y, r1, g1, b1, r2, g2, b2);
                    }

                    diff = diff + data;
                }
            }

            if (diff == 0) {
                log.info("Both images ({} and {}) are identical!", imageName1, imageName2);
            }
        }
    }
}
