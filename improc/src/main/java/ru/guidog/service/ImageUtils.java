package ru.guidog.service;

import java.awt.image.BufferedImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import static org.bytedeco.javacpp.opencv_core.*;

/**
 *
 * @author ScorpDS
 */
public class ImageUtils {

    private static final Logger log = LogManager.getLogger(ImageUtils.class);

    private static final OpenCVFrameConverter.ToIplImage OPENCV_CONVERTER = new OpenCVFrameConverter.ToIplImage();

    public static void show(opencv_core.IplImage toShow, String windowTitle) {
        final CanvasFrame canvas_bin = new CanvasFrame(windowTitle);
        canvas_bin.showImage(OPENCV_CONVERTER.convert(toShow));
    }

    public static void show(opencv_core.Mat imageToShow, String windowTitle) {
        final CanvasFrame canvas_bin = new CanvasFrame(windowTitle);
        canvas_bin.showImage(OPENCV_CONVERTER.convert(new opencv_core.IplImage(imageToShow)));
    }

    public static IplImage convertBufferedToIpl(BufferedImage source) {
        Java2DFrameConverter frameBufferedImageConverter = new Java2DFrameConverter();
        //2.2 gamma was found experimentally, sort of magic val here
        Frame screenshotFrame = frameBufferedImageConverter.getFrame(source, 2.2, false);
        CanvasFrame canvas_bin = new CanvasFrame("test");
        canvas_bin.showImage(screenshotFrame);
        IplImage result = OPENCV_CONVERTER.convert(screenshotFrame);

        return result;
    }

    public static void writeImageToDisk(Mat image, String filename) {
        log.info("Saving " + filename);
        if (null == filename || filename.isEmpty()) {
            log.error("Incorrect or empty file name.");
        }
        imwrite(filename, image);
    }

    public static IplImage convToGray(IplImage src) {
        opencv_core.CvSize dim = new opencv_core.CvSize(src.width(), src.height());
        IplImage dst = IplImage.create(dim, src.depth(), 1);
        cvCvtColor(src, dst, CV_BGR2GRAY);
        return dst;
    }

}
