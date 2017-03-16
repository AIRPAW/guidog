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
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.opencv_features2d;
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
    private IplImage shapes;

    private Mat binar = new Mat();

    public ImageDecomposition() {
        SAVE_ON_DISK = Guide.saveImages();
        SHOW_IMAGES = Guide.showImages();
        STORAGE_PATH = System.getenv("HOME") + Guide.getConfig().getProperty("storage.shared");
        CONTOURS_PATH = Guide.getConfig().getProperty("storage.contours");
        RESIZED_PATH = Guide.getConfig().getProperty("storage.output");
        resizeX = Integer.parseInt(Guide.getConfig().getProperty("image.size.x"));
        resizeY = Integer.parseInt(Guide.getConfig().getProperty("image.size.y"));
    }

    public String getStoragePath() {
        return STORAGE_PATH;
    }

    public SuspectsList detectObjects(IplImage transformed) {
        if (SHOW_IMAGES) {
            ImageUtils.show(transformed, "Transformed");
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
            ImageUtils.show(withContours, "Result");
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

        originalImg = ImageUtils.convertBufferedToIpl(screenshot);

        Mat convert = new Mat(originalImg);
        originalImg = new IplImage(convert);
        ImageUtils.show(originalImg, "Original");

        Mat mat = new Mat(ImageUtils.convToGray(originalImg));
        Mat test = mat.clone();

        if (SAVE_ON_DISK) {
            ImageUtils.writeImageToDisk(mat, "recievedScreenshot.jpg");
        }

        log.info("Starting screenshot thresholding..");
        adaptiveThreshold(mat, mat, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 5, 1);
        log.info("Thresholding done!");

        if (SAVE_ON_DISK) {
            ImageUtils.writeImageToDisk(mat, "thresholded.jpg");
        }
        if (SHOW_IMAGES) {
            ImageUtils.show(originalImg, "original");
            ImageUtils.show(new IplImage(binar), "threshholded");
        }

        log.info("Starting screenshot morphological transformation..");
//        mat = morphologicalTransformation(mat);
        log.info("Transformation done!");

        if (SAVE_ON_DISK) {
            ImageUtils.writeImageToDisk(mat, "transformed.jpg");
        }
//        return detectObjects(new IplImage(mat));
        return findMser(test);
    }

    private Mat morphologicalTransformation(Mat source) {
        Mat result = new Mat();
        Mat open = new Mat();

        Mat morphKernel = getStructuringElement(MORPH_CROSS, new Size(2, 2));
        morphologyEx(source, result, MORPH_OPEN, morphKernel);

//        Mat morphKernel = getStructuringElement(MORPH_RECT, new Size(3, 2));
//        morphologyEx(source, result, MORPH_CLOSE, morphKernel);
        return result;
    }

    public SuspectsList findMser(Mat image) {
        ImageUtils.show(image, "BeforeMSER");
        shapes = new IplImage(image);

        SuspectsList list = new SuspectsList();

        //TODO: need to tune this parameters
        opencv_features2d.MSER mser = opencv_features2d.MSER.create(2, 50, 1000, 0.25, 0.2, 100, 1.01, 0.003, 5);

        PointVectorVector kpvv = new PointVectorVector();
        RectVectorExt rects = new RectVectorExt();
        mser.detectRegions(image, kpvv, rects);
        rects.sync();

        for (int i = 0; i < rects.size() - 1; i++) {
            for (int j = i + 1; j < rects.size(); j++) {

                Rect r1 = rects.get(i);
                Rect r2 = rects.get(j);
                if (containing(r1, r2)) {
                    //TODO: implement contours processing for cases of intersection and nesting
                } else if (rectsIntersect(r1, r2)) {
                    //TODO: see above
                }
            }
        }

        int i = 0;
        for (Rect rect : rects.getRectList()) {
            i++;
            cropAndStore(image, rect, i);
        }

        ImageUtils.show(shapes, "MSER");
        return list;
    }

    private void cropAndStore(Mat src, Rect rect, int i) {

        cvRectangle(shapes,
                cvPoint(rect.x(), rect.y()),
                cvPoint(rect.x() + rect.width(), rect.y() + rect.height()),
                cvScalar(0, 255, 0, 0), 1, 0, 0);

        cvSetImageROI(shapes, cvRect(rect.x(), rect.y(), rect.width(), rect.height()));

        if (SAVE_ON_DISK) {
            cvSaveImage(CONTOURS_PATH + i + ".jpg", shapes);
        }

        Mat black = new Mat();
        black.create(resizeY, resizeX, CV_8U);
        Scalar sc0 = new Scalar(0);
        for (int k = 0; k < black.rows(); k++) {
            for (int j = 0; j < black.cols(); j++) {
                black.put(sc0);
            }
        }

        Rect result_rec = new Rect(cvGetImageROI(shapes));
        Mat tmp = new Mat(src, result_rec);

        if (rect.width() > resizeX) {
            resize(tmp, tmp, new Size(resizeX, tmp.rows()), 0, 0, INTER_LINEAR);
        }
        if (rect.height() > resizeY) {
            resize(tmp, tmp, new Size(tmp.cols(), resizeY), 0, 0, INTER_LINEAR);
        }
        int smallPictX = resizeX / 2 - tmp.cols() / 2;
        int smallPictY = resizeY / 2 - tmp.rows() / 2;
        tmp.copyTo(black.rowRange(smallPictY, (smallPictY + tmp.rows())).colRange(smallPictX, smallPictX + tmp.cols()));

        if (SAVE_ON_DISK) {
            imwrite(RESIZED_PATH + i + ".jpg", black);
        }

        imwrite(STORAGE_PATH + i + ".jpg", black);

        cvResetImageROI(shapes);
    }

    private boolean rectsIntersect(Rect r1, Rect r2) {

        if (r1.contains(r2.tl())
                || r1.contains(r2.br())
                || r2.contains(r1.tl())
                || r2.contains(r1.br())) {
            return true;
        }
        if (r1.x() >= r2.x() && r1.y() <= r2.y()) {
            if (r1.x() + r1.width() <= r2.x() + r2.width()
                    && r1.y() + r1.height() > r2.y() + r2.height()) {
                return true;
            }
        }
        return false;
    }

    //TODO: fix and implement
    private void joinContours(Rect r1, Rect r2) {
        int tlXmin, brXmax, tlYmin, brYmax;

        tlXmin = (r1.tl().x() < r2.tl().x()) ? r1.tl().x() : r2.tl().x();
        tlYmin = (r1.tl().y() < r2.tl().y()) ? r1.tl().y() : r2.tl().y();
        brXmax = (r1.br().x() > r2.br().x()) ? r1.br().x() : r2.br().x();
        brYmax = (r1.br().y() > r2.br().y()) ? r1.br().y() : r2.br().y();
        r1.tl().x(tlXmin).y(tlYmin);
        r1.br().x(brXmax).y(brYmax);
    }

    private boolean containing(Rect outer, Rect inner) {
        if (outer.contains(inner.tl()) && outer.contains(inner.br())) {
            return true;
        }
        return false;
    }
}
