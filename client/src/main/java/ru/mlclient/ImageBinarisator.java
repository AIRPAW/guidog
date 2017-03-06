/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.mlclient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *
 * @author sbt-ulyanov-ka
 */
public class ImageBinarisator {

    private BufferedImage img;

    public ImageBinarisator setImage(BufferedImage img) {
        this.img = img;
        return this;
    }

    public ImageBinarisator convertToGrayscale() {
        Graphics g = img.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return this;
    }

    public ImageBinarisator setThreshold(int threshold, boolean inverted) {
        int white = (new Color(255, 255, 255)).getRGB();
        int black = (new Color(0, 0, 0)).getRGB();
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int pixel = img.getRaster().getSample(i, j, 0);
                if (!inverted) {
                    if (pixel < threshold) {
                        img.setRGB(i, j, black);
                    } else {
                        img.setRGB(i, j, white);
                    }
                } else if (pixel > threshold) {
                    img.setRGB(i, j, black);
                } else {
                    img.setRGB(i, j, white);
                }

            }
        }
        return this;
    }

    public BufferedImage getImage() {
        return img;
    }

}
