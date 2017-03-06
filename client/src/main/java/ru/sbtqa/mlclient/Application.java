/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sbtqa.mlclient;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author sbt-ulyanov-ka
 */
public class Application {

    public static void main(String[] argv) {
        int threshold = 230;
        boolean inverted = false;
        boolean thresholded = true;
        String elementType="", elementText = "";
        for (String var : argv) {
            if (var.equals("-inv")) {
                inverted = true;
                continue;
            }
            if (var.equals("-noTh")) {
                thresholded = false;
                continue;
            }
            if (var.contains("-thv")) {
                threshold = Integer.parseInt(getValueOpt(var, "-thv"));
                continue;
            }
            if (var.contains("-etype")) {
                elementType = getValueOpt(var, "-etype");   
                continue;
            }
            if (var.contains("-etext")) { 
                elementText = getValueOpt(var, "-etext");
            }
        }
        try {
            Robot robot = new Robot();
            Rectangle screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage bImage = robot.createScreenCapture(screenRectangle);

            if (thresholded) {
                ImageBinarisator bin = new ImageBinarisator();
                bin.setImage(bImage).convertToGrayscale().setThreshold(threshold, inverted);
                bImage = bin.getImage();
            }

            RawScreenshot image = new RawScreenshot(bImage);

            RequestBuilder request = new RequestBuilder();
            request.sendRequest(image,elementType,elementText);
        } catch (AWTException | HeadlessException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String getValueOpt(String fullArg, String qualifier) {
        if (fullArg.contains(qualifier)) {
            if (fullArg.contains("=")) {
                String[] parts = fullArg.split("=");
                if (parts.length == 2) {
                    return parts[1];
                }
            }
        }
        throw new IllegalArgumentException("Cannot do anything with argument " + fullArg);
    }

}
