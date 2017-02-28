package ru.scorpds.guidog.service;

import java.io.File;
import java.util.HashMap;
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
import java.awt.Point;

/**
 *
 * @author scorp
 */
public class ImageDecomposition {

    static final short WHITE = 255, BLACK = 0;

    public static HashMap detectObjects(IplImage srcImage, IplImage transformed) {
        CanvasFrame canvas_bin = new CanvasFrame("Transformed");
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        canvas_bin.showImage(converter.convert(srcImage));

        IplImage resultImage = cvCreateImage(srcImage.cvSize(), IPL_DEPTH_8U, 3);
        cvCvtColor(srcImage, resultImage, CV_GRAY2BGR);

        CvMemStorage mem = CvMemStorage.create();
        CvSeq contours = new CvSeq();

        cvFindContours(transformed, mem, contours, Loader.sizeof(CvContour.class), CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
        CvRect boundbox;

        IplImage shapes = cvCreateImage(srcImage.cvSize(), IPL_DEPTH_8U, 3);
        Mat result_mat = new Mat(srcImage);

        File imgDir = new File("img/");
        if (!imgDir.exists() && !imgDir.mkdir()) {
            System.err.println("Cannot create img/ folder");
        }
        File outputDir = new File("output/");
        if (!outputDir.exists() && !outputDir.mkdir()) {
            System.err.println("Cannot create output/ folder");
        }

        int i = 0;
        HashMap<Integer, Point> coordinates = new HashMap<>();
        for (CvSeq ptr = contours; ptr != null; ptr = ptr.h_next()) {
            boundbox = cvBoundingRect(ptr, 0);

            cvRectangle(resultImage, cvPoint(boundbox.x(), boundbox.y()),
                    cvPoint(boundbox.x() + boundbox.width(), boundbox.y() + boundbox.height()),
                    cvScalar(0, 255, 0, 0), 1, 0, 0);

            cvRectangle(shapes, cvPoint(boundbox.x(), boundbox.y()),
                    cvPoint(boundbox.x() + boundbox.width(), boundbox.y() + boundbox.height()),
                    cvScalar(0, 255, 0, 0), 1, 0, 0);

            cvSetImageROI(srcImage, cvRect(boundbox.x(), boundbox.y(), boundbox.width(), boundbox.height()));

            cvSaveImage("img/" + i + ".jpg", srcImage);

            if (i != 0) {
                Rect result_rec = new Rect(cvGetImageROI(srcImage));
                int w = result_rec.width();
                int h = result_rec.height();
                if (w == 0 || h == 0) {
                    continue;
                }

                Mat to_save = new Mat(result_mat, result_rec);

                //ресайз с параметрами 200х30
                int hDest = 30;
                int wDest = 200;

                Mat black = new Mat();
                black.create(hDest, wDest, CV_8U);
                Scalar sc0 = new Scalar(0);
                for (int k = 0; k < black.rows(); k++) {
                    for (int j = 0; j < black.cols(); j++) {
                        black.put(sc0);
                    }
                }

                Mat tmp = to_save.clone();
                if (w > wDest) {
                    resize(tmp, tmp, new Size(wDest, tmp.rows()), 0, 0, INTER_LINEAR);
                }
                if (h > hDest) {
                    resize(tmp, tmp, new Size(tmp.cols(), hDest), 0, 0, INTER_LINEAR);
                }
                int smallPictX = wDest / 2 - tmp.cols() / 2;
                int smallPictY = hDest / 2 - tmp.rows() / 2;
                tmp.copyTo(black.rowRange(smallPictY, (smallPictY + tmp.rows())).colRange(smallPictX, smallPictX + tmp.cols()));

                imwrite("output/" + i + ".jpg", black);
                cvResetImageROI(srcImage);                
            }
            int contourCenterX = boundbox.x() + boundbox.width() / 2;
            int contourCenterY = boundbox.y() + boundbox.height() / 2;
            Point p = new Point(contourCenterX, contourCenterY);

            coordinates.put(i, p);
            i++;
        }
        for (Integer key : coordinates.keySet()) {
            System.out.println("Image: " + key + ".jpg, Coordinates of center: " + ((Point) coordinates.get(key)).x+ ", " + ((Point) coordinates.get(key)).y);
        }
//        final CanvasFrame canvas = new CanvasFrame("Demo");
//
//        /* показываем картинку в нашем фрейме */
//        canvas.showImage(converter.convert(resultImage));
        return coordinates;
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

    public static HashMap notMain(String path) {

        /* открываем картинку */
        IplImage image = cvLoadImage(path, CV_LOAD_IMAGE_GRAYSCALE);
        Mat mat = new Mat(image);

        final CanvasFrame canvas_bin = new CanvasFrame("Bin");
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        canvas_bin.showImage(converter.convert(image));

        IplImage dst = new IplImage(morphologicalTransformation(mat));
        
        return detectObjects(image, dst);
    }

}
