package ru.guidog.service;

import java.io.File;
import org.bytedeco.javacpp.Loader;
import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import ru.guidog.Guide;
import ru.guidog.model.Suspect;
import ru.guidog.model.SuspectsList;

/**
 *
 * @author scorpds
 */
public class ImageDecomposition {
    
    Logger log = LogManager.getLogger(this.getClass());

    private final boolean SAVE_ON_DISK;
    private final boolean SHOW_IMAGES;
    private final String STORAGE_PATH;
    private final String CONTOURS_PATH;
    private final String RESIZED_PATH;

    private final int resizeX;
    private final int resizeY;

    private IplImage originalImg;

    private Mat binar = new Mat();

    private OpenCVFrameConverter.ToIplImage cvConverter;

    public ImageDecomposition() {
        SAVE_ON_DISK = Guide.saveImages();
        SHOW_IMAGES = Guide.showImages();
        STORAGE_PATH = System.getenv("HOME") + Guide.getConfig().getProperty("storage.shared");
        CONTOURS_PATH = Guide.getConfig().getProperty("storage.contours");
        RESIZED_PATH = Guide.getConfig().getProperty("storage.output");
        resizeX = Integer.parseInt(Guide.getConfig().getProperty("image.size.x"));
        resizeY = Integer.parseInt(Guide.getConfig().getProperty("image.size.y"));

        cvConverter = new OpenCVFrameConverter.ToIplImage();
    }

    public String getStoragePath() {
        return STORAGE_PATH;
    }

    public SuspectsList detectObjects(IplImage transformed) {
        if (SHOW_IMAGES) {
            showImage(transformed, "Transformed");
        }

        IplImage withContours = originalImg.clone();

        CvMemStorage mem = CvMemStorage.create();
        CvSeq contours = new CvSeq();
        
        log.info("Starting FindContours procedure..");
        cvFindContours(transformed, mem, contours, Loader.sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE, cvPoint(-2, -2));
        log.info("Contours finding finished!");

        if (SAVE_ON_DISK) {
            File imgDir = new File(CONTOURS_PATH);
            if (!imgDir.exists() && !imgDir.mkdir()) {
                System.err.println("Cannot create " + CONTOURS_PATH + " folder");
            }
            File outputDir = new File(RESIZED_PATH);
            if (!outputDir.exists() && !outputDir.mkdir()) {
                System.err.println("Cannot create " + RESIZED_PATH + " folder");
            }
        } else {
            File path = new File(STORAGE_PATH);
            if (!path.exists() && !path.mkdir()) {
                System.err.println("Cannot create " + STORAGE_PATH + " folder");
            }
        }

        SuspectsList imgList = getContours(withContours, contours);

        if (SHOW_IMAGES) {
            showImage(withContours, "Result");
        }

        return imgList;
    }

    private SuspectsList getContours(IplImage withContours, CvSeq contours) {
        int i = 0;
        CvRect boundbox;
        SuspectsList list = new SuspectsList();
        IplImage shapes = cvCreateImage(originalImg.cvSize(), IPL_DEPTH_8U, 3);
        for (CvSeq ptr = contours; ptr != null; ptr = ptr.h_next()) {
            log.info("Starting contour image cropping, resizing and saving.." + i);
            boundbox = cvBoundingRect(ptr, 0);

            cvRectangle(withContours, cvPoint(boundbox.x(), boundbox.y()),
                    cvPoint(boundbox.x() + boundbox.width(), boundbox.y() + boundbox.height()),
                    cvScalar(0, 255, 0, 0), 1, 0, 0);

            cvRectangle(shapes, cvPoint(boundbox.x(), boundbox.y()),
                    cvPoint(boundbox.x() + boundbox.width(), boundbox.y() + boundbox.height()),
                    cvScalar(0, 255, 0, 0), 1, 0, 0);

            cvSetImageROI(originalImg, cvRect(boundbox.x(), boundbox.y(), boundbox.width(), boundbox.height()));

            if (SAVE_ON_DISK) {
                cvSaveImage(CONTOURS_PATH + i + ".jpg", originalImg);
            }

            Rect result_rec = new Rect(cvGetImageROI(originalImg));
            int w = result_rec.width();
            int h = result_rec.height();
            if (w == 0 || h == 0) {
                continue;
            }

            Mat to_save = new Mat(binar, result_rec);

            Mat black = new Mat();
            black.create(resizeY, resizeX, CV_8U);
            Scalar sc0 = new Scalar(0);
            for (int k = 0; k < black.rows(); k++) {
                for (int j = 0; j < black.cols(); j++) {
                    black.put(sc0);
                }
            }

            Mat tmp = to_save.clone();
            if (w > resizeX) {
                resize(tmp, tmp, new Size(resizeX, tmp.rows()), 0, 0, INTER_LINEAR);
            }
            if (h > resizeY) {
                resize(tmp, tmp, new Size(tmp.cols(), resizeY), 0, 0, INTER_LINEAR);
            }
            int smallPictX = resizeX / 2 - tmp.cols() / 2;
            int smallPictY = resizeY / 2 - tmp.rows() / 2;
            tmp.copyTo(black.rowRange(smallPictY, (smallPictY + tmp.rows())).colRange(smallPictX, smallPictX + tmp.cols()));
            if (SAVE_ON_DISK) {
                imwrite(RESIZED_PATH + i + ".jpg", black);
            }
            imwrite(STORAGE_PATH + i + ".jpg", black);
            list.add(new Suspect(new IplImage(black)));

            cvResetImageROI(originalImg);
            int contourCenterX = boundbox.x() + boundbox.width() / 2;
            int contourCenterY = boundbox.y() + boundbox.height() / 2;
            if (list.size() > 0) {
                list.getLast().setCoords(contourCenterX, contourCenterY);
                list.getLast().setPath(i + ".jpg");
            }

            i++;
        }
        log.info("All images saved!");
        return list;
    }

    public SuspectsList batchedProcessing(BufferedImage screenshot) throws FileNotFoundException, IOException {
        log.info("Starting image processing..");
        Java2DFrameConverter frameBufferedImageConverter = new Java2DFrameConverter();
        Frame screenshotFrame = frameBufferedImageConverter.convert(screenshot);

        originalImg = cvConverter.convert(screenshotFrame);

        Mat mat = new Mat(convToGray(originalImg));
        
        if (SAVE_ON_DISK) {
            log.info("Saving original screenshot..");
            imwrite("recievedScreenshot.jpg", mat);
            log.info("Done saving!");
        }
        
        log.info("Starting screenshot thresholding..");
        adaptiveThreshold(mat, binar, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 15, 2);
        log.info("Thresholding done!");
        
        if (SAVE_ON_DISK) {
            log.info("Saving thresholded screenshot..");
            imwrite("thresholded.jpg", binar);
            log.info("Done saving!");
        }
        if (SHOW_IMAGES) {
            showImage(originalImg, "original");
            showImage(new IplImage(binar), "threshholded");
        }
        log.info("Starting screenshot morphological transformation..");
        mat = morphologicalTransformation(binar);
        log.info("Transformation done!");

        if (SAVE_ON_DISK) {
            log.info("Saving transformed screenshot..");
            imwrite("transformed.jpg", mat);
            log.info("Done saving!");
        }
        return detectObjects(new IplImage(mat));
    }

    private Mat morphologicalTransformation(Mat source) {
        Mat result = new Mat();
        Mat open = new Mat();

        Mat morphKernel = getStructuringElement(MORPH_CROSS, new Size(2, 2));
        morphologyEx(source, open, MORPH_OPEN, morphKernel);

        morphKernel = getStructuringElement(MORPH_RECT, new Size(7, 2));
        morphologyEx(open, result, MORPH_CLOSE, morphKernel);

        return result;
    }

    public IplImage convToGray(IplImage src) {
        CvSize dim = new CvSize(src.width(), src.height());
        IplImage dst = IplImage.create(dim, src.depth(), 1);
        cvCvtColor(src, dst, CV_BGR2GRAY);
        return dst;
    }

    public void showImage(IplImage toShow, String windowTitle) {
        final CanvasFrame canvas_bin = new CanvasFrame(windowTitle);
        canvas_bin.showImage(cvConverter.convert(toShow));
    }
}
