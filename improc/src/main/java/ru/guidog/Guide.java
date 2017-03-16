package ru.guidog;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import ru.guidog.service.Binarization;
import ru.guidog.service.ImageDecomposition;

/**
 *
 * @author scorpds
 */
//@SpringBootApplication
public class Guide {

    private static Logger log = LogManager.getLogger(Guide.class);

    private static final Properties CONFIG = new Properties();
    private static boolean writeImgOnDisk;
    private static boolean showImagesFrames;

    static {
        try {
            CONFIG.load(Guide.class.getClassLoader().getResourceAsStream("application.properties"));
            writeImgOnDisk = Guide.CONFIG.getProperty("saveImagesOnDisk").equalsIgnoreCase("true");
            showImagesFrames = Guide.CONFIG.getProperty("showImages").equalsIgnoreCase("true");

        } catch (IOException ex) {
            log.log(Level.ERROR, ex);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
        Robot robot = new Robot();
        Rectangle screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage bImage = robot.createScreenCapture(screenRectangle);
        BufferedImage converted = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        converted.getGraphics().drawImage(bImage, 0, 0, null);
//        converted.getGraphics().dispose();
//        bImage = convertRGBAToIndexed(bImage);
//        Binarization.show(bImage);
//        SpringApplicationBuilder b = new SpringApplicationBuilder(Guide.class);
//        b.headless(false).properties().run(args);

//        Binarization bin = new Binarization(bImage);
//        bin.teast();        
        ImageDecomposition decode = new ImageDecomposition();
        decode.batchedProcessing(converted);

    }

    public static boolean saveImages() {
        return writeImgOnDisk;
    }

    public static Properties getConfig() {
        return CONFIG;
    }

    public static boolean showImages() {
        return showImagesFrames;
    }

    public static BufferedImage convertRGBAToIndexed(BufferedImage src) {
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
//        Graphics g = dest.getGraphics();
//        g.setColor(new Color(231, 20, 189));
//
////        // fill with a hideous color and make it transparent
////        g.fillRect(0, 0, dest.getWidth(), dest.getHeight());
        dest = makeTransparent(dest, 0, 0);

        dest.createGraphics().drawImage(src, 0, 0, null);
        return dest;
    }

    public static BufferedImage makeTransparent(BufferedImage image, int x, int y) {
        ColorModel cm = image.getColorModel();
        if (!(cm instanceof IndexColorModel)) {
            return image; // sorry...
        }
        IndexColorModel icm = (IndexColorModel) cm;
        WritableRaster raster = image.getRaster();
        int size = icm.getMapSize();
        byte[] reds = new byte[size];
        byte[] greens = new byte[size];
        byte[] blues = new byte[size];
        icm.getReds(reds);
        icm.getGreens(greens);
        icm.getBlues(blues);
        IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens, blues);
        return new BufferedImage(icm2, raster, image.isAlphaPremultiplied(), null);
    }

}
