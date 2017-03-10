/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.guidog.service;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author sbt-ulyanov-ka
 */
public class Binarization {

    private BufferedImage image;
    private int frameWidth = 240;
    private int frameHeight = 180;

    public Binarization(BufferedImage image) {
        this.image = image;
    }

    public void test() {
        int xTileNum = (image.getWidth() / frameWidth);
        if (frameWidth * xTileNum < image.getWidth()) {
            xTileNum++;
        }
        int yTileNum = image.getHeight() / frameHeight;
        if (frameHeight * yTileNum < image.getHeight()) {
            yTileNum++;
        }
        for (int i = 0; i < xTileNum; i++) {
            for (int j = 0; j < yTileNum; j++) {
                Raster frame = image.getData(new Rectangle(i * frameWidth, j * frameHeight, frameWidth, frameHeight));
                getMinAvg(frame); //                show(frame);
            }
        }
    }

    private void getMinAvg(Raster frame) {
        double[] pixel = null;
        double pixelBrigthness;
        for (int i = 0; i < frameWidth; i++) {
            for (int j = 0; j < frameHeight; j++) {
                pixel = frame.getPixel(i, j, pixel);        
                pixelBrigthness = (pixel[0] * 0.2126f + pixel[1] * 0.7152f + pixel[2] * 0.0722f) / 255;
            }
        }
    }

    private void show(Raster raster) {
        BufferedImage img = new BufferedImage(frameWidth, frameHeight, image.getType());
        img.setData(Raster.createRaster(raster.getSampleModel(), raster.getDataBuffer(), new Point(0, 0)));
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(img));
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(label);
        frame.add(panel);
        frame.setSize(img.getWidth(), img.getHeight());
        frame.setVisible(true);

    }
}
