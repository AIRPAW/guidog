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
        if (argv.length>0 && !argv[0].isEmpty()) {
            threshold = Integer.parseInt(argv[0]);
        }
        if (argv.length>1 && !argv[1].isEmpty()) {
            if (argv[1].equalsIgnoreCase("inv")) {
                inverted = true;
            }
        }
        try {
            Robot robot = new Robot();
            Rectangle screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage bImage = robot.createScreenCapture(screenRectangle);

            ImageBinarisator bin = new ImageBinarisator();
            bin.setImage(bImage).convertToGrayscale().setThreshold(threshold,inverted);
            bImage = bin.getImage();
            RawScreenshot image = new RawScreenshot(bImage);

            RequestBuilder request = new RequestBuilder();
            request.sendRequest(image);
        } catch (AWTException | HeadlessException | IOException e) {
            e.printStackTrace();
        }
//        }
    }

}
