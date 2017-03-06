package ru.guidog.service;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract;

/**
 *
 * @author scorpds
 */
public class CharactersRecognition {

    private static tesseract.TessBaseAPI tesseract = new tesseract.TessBaseAPI();
    private static PIX pic = null;
    private static String lang = null;

    private CharactersRecognition() {
    }

    public static String getLang() {
        return lang;
    }

    public static void setLang(String lang) {
        CharactersRecognition.lang = lang;
    }

    public static void setPic(String path) {
        pic = lept.pixRead(path);
    }

    public static void setPic(BytePointer pointer) {
        pic = lept.pixRead(pointer);
    }

    public static String getTextFromPic() {
        String result = null;

        System.out.println(System.getenv("TESSDATA_PREFIX"));

        tesseract.Init(null, lang);
        tesseract.SetImage(pic);
        BytePointer textPointer = tesseract.GetUTF8Text();
        result = textPointer.getString();

        textPointer.deallocate();
        lept.pixDestroy(pic);

        return result;
    }

    public static String getTextFromPic(BytePointer picBytePointer) {
        String result = null;

        PIX pic = lept.pixRead(picBytePointer);
        tesseract.SetImage(pic);
        BytePointer textPointer = tesseract.GetUTF8Text();
        result = textPointer.getString();

        textPointer.deallocate();
        lept.pixDestroy(pic);

        return result;
    }

}
