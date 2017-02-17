package ru.scorpds.guidog.service;

import java.io.File;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.indexer.UByteIndexer;
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

/**
 *
 * @author sbt-voronova-id
 */
public class ImageDecomposition {

    static final short white = 255, black = 0;

    public static IplImage detectObjects(IplImage srcImage) {

        IplImage resultImage = cvCreateImage(srcImage.cvSize(), IPL_DEPTH_8U, 3);
        cvCvtColor(srcImage, resultImage, CV_GRAY2BGR);

        CvMemStorage mem = CvMemStorage.create();
        CvSeq contours = new CvSeq();
        CvSeq ptr = new CvSeq();

        cvFindContours(srcImage, mem, contours, Loader.sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
        CvRect boundbox;

        IplImage shapes = cvCreateImage(srcImage.cvSize(), IPL_DEPTH_8U, 3);
        Mat result_mat = new Mat(srcImage);
        File imgDir = new File("target\\img");
        if (!imgDir.exists() && !imgDir.mkdir()) {
            System.err.println("Cannot create \\target\\img folder");
        }
        File outputDir = new File("target\\output");
        if (!outputDir.exists() && !outputDir.mkdir()) {
            System.err.println("Cannot create \\target\\output folder");
        }
        int i = 0;
        for (ptr = contours; ptr != null; ptr = ptr.h_next()) {
            boundbox = cvBoundingRect(ptr, 0);

            cvRectangle(resultImage, cvPoint(boundbox.x(), boundbox.y()),
                    cvPoint(boundbox.x() + boundbox.width(), boundbox.y() + boundbox.height()),
                    cvScalar(0, 255, 0, 0), 1, 0, 0);

            cvRectangle(shapes, cvPoint(boundbox.x(), boundbox.y()),
                    cvPoint(boundbox.x() + boundbox.width(), boundbox.y() + boundbox.height()),
                    cvScalar(0, 255, 0, 0), 1, 0, 0);

            cvSetImageROI(srcImage, cvRect(boundbox.x(), boundbox.y(), boundbox.width(), boundbox.height()));

            cvSaveImage("target\\img\\" + i + ".jpg", srcImage);
            CvRect rect = cvGetImageROI(srcImage);
            if (i != 0) {
                Rect result_rec = new Rect(cvGetImageROI(srcImage));
                int w = result_rec.width();
                int h = result_rec.height();
                if (w == 0 || h == 0) {
                    continue;
                }

                Mat to_save = new Mat(result_mat, result_rec);

                //ресайз с параметрами 200х30
                int def_h = 30;
                int def_w = 200;
                int place_w = def_w / 2 - w / 2;
                int place_h = def_h / 2 - h / 2;
                Mat def_mat = new Mat();
                def_mat.create(def_h, def_w, 1);
                Scalar sc0 = new Scalar(0);
                for (int k = 0; k < def_mat.rows(); k++) {
                    for (int j = 0; j < def_mat.cols(); j++) {
                        def_mat.put(sc0);
                    }
                }
                Mat tmp = new Mat();
                tmp = to_save.clone();
                if (w > def_w) {
                    tmp.resize(def_w);
                }
                if (h > def_h) {
                    tmp.t();
                    tmp.resize(def_h);
                    tmp.t();
                }
                if (w < def_w && h < def_h) {
                    tmp.copyTo(def_mat.rowRange(place_h, (place_h + h)).colRange(place_w, (place_w + w)));
                }
                imwrite("target\\output\\" + i + ".jpg", def_mat);
                cvResetImageROI(srcImage);
            }
            i++;
        }

//        CanvasFrame canvas_bin = new CanvasFrame("Shapes");
//        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
//        canvas_bin.showImage(converter.convert(shapes));
        return resultImage;
    }

    private static Mat morphologicalTransformation(Mat source) {
        Mat result = new Mat();
        Mat grad = new Mat();
        Mat morphKernel = getStructuringElement(MORPH_ELLIPSE, new Size(2, 2));
        morphologyEx(source, grad, MORPH_GRADIENT, morphKernel);
        morphKernel = getStructuringElement(MORPH_RECT, new Size(7, 1));
        morphologyEx(grad, result, MORPH_CLOSE, morphKernel);
        return result;
    }

    public static Mat RLS(Mat source, int T) {
        Mat resW = source.clone();
        short fill;
        UByteIndexer indSource = source.createIndexer();
        UByteIndexer indResult = resW.createIndexer();
        for (int i = 0; i < source.rows(); i++) {
            int k = 0;
            while (k < source.cols()) {
                boolean isWhite = true;
                int counter = 0;

                while (isWhite && (k + counter) < source.cols()) {
                    if (indSource.get(i, k + counter) == black) {
                        isWhite = false;
                    }
                    counter++;
                }
                fill = counter >= T ? black : white;
                for (int j = k; j < k + counter; j++) {
                    indResult.put(i, j, fill);
                }
                k += counter;
            }
        }
        return resW;
    }

    public static boolean isRectBlack(Rect rect, Mat source) {
        Size sz = new Size(rect.width(), rect.height());
        Mat blackMat = new Mat(rect.width(), rect.height(), CV_8U, new Scalar(black));
        Mat checkMat = new Mat(source, rect);
        UByteIndexer indResult = checkMat.createIndexer();
        Scalar checkSum = sumElems(blackMat);
        Scalar currentSum = sumElems(checkMat);
        return (checkSum.get(0) - currentSum.get(0)) > 1e-5;
    }

    public static Mat xyCut(Mat source) {
        int core = 5;
        int size = core;
        IplImage result_img = new IplImage(source);
        Mat blackMat = new Mat(size, size, CV_8U, new Scalar(black));
        Rect rect;
        int tmp1 = source.cols();
        int tmp2 = source.rows();

        for (int i = 0; i < (source.rows() - size); i += (size + 1)) {
            for (int j = 0; j < (source.cols() - size); j += (size + 1)) {
                cvSetImageROI(result_img, cvRect(i, j, size, size));
                rect = new Rect(cvGetImageROI(result_img));
                System.out.println(i + " " + j);
                if (isRectBlack(rect, source)) {

                    //blackMat.copyTo(source.rowRange(i, i + size).colRange(j, j + size));
                }
                cvResetImageROI(result_img);
            }
        }
        return source;
    }

    public static void notMain(String path) {

        /* открываем картинку */
        IplImage image = cvLoadImage(path, CV_LOAD_IMAGE_GRAYSCALE);

        Mat dst_mat = new Mat(image.asCvMat());
        final CanvasFrame canvas_bin = new CanvasFrame("Bin");
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        canvas_bin.showImage(converter.convert(dst_mat));

//        Mat src_mat = new Mat(image.asCvMat());
//        adaptiveThreshold(dst_mat, src_mat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 2);
        IplImage dst = new IplImage(morphologicalTransformation(dst_mat));
//        IplImage dst = new IplImage(dst_mat);     

        IplImage resultRLS_img = new IplImage(dst);

        IplImage curve_img = detectObjects(resultRLS_img);

        final CanvasFrame canvas = new CanvasFrame("Demo");

        /* показываем картинку в нашем фрейме */
        canvas.showImage(converter.convert(curve_img));
//        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

}
