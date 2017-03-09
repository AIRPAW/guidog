package ru.guidog;

import ru.guidog.model.SuspectsList;
import ru.guidog.model.Suspect;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import ru.guidog.service.CharactersRecognition;
import ru.guidog.service.ImageDecomposition;

/**
 *
 * @author scorpds
 */
@SpringBootApplication
public class Guide {

    private static final Properties CONFIG = new Properties();
    private static boolean writeImgOnDisk;
    private static boolean showImagesFrames;;

    static {
        try {
            CONFIG.load(Guide.class.getClassLoader().getResourceAsStream("config.properties"));
            writeImgOnDisk = Guide.CONFIG.getProperty("saveImagesOnDisk").equalsIgnoreCase("true");
            showImagesFrames = Guide.CONFIG.getProperty("showImages").equalsIgnoreCase("true");
            
        } catch (IOException ex) {
            Logger.getLogger(Guide.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplicationBuilder b = new SpringApplicationBuilder(Guide.class);
        b.headless(false).properties("application.properties").run(args);

//        runMain();
    }

    public static void singlePicOCR() {
        CharactersRecognition.setLang("eng");
        CharactersRecognition.setPic("output/text.png");
        String elementText = CharactersRecognition.getTextFromPic();

        System.out.println(elementText);
    }

    public static void runMain() throws IOException, InterruptedException {

        BufferedImage img = ImageIO.read(new File("grey.png"));
        ImageDecomposition decomp = new ImageDecomposition();

        SuspectsList imgList = decomp.batchedProcessing(img);

        SuspectsList list = new SuspectsList();
        for (Suspect suspect : imgList) {
            list.add(runTorch(suspect.getPath()));
            list.getLast().setCurImg(suspect.getCurImg());
        }

        Suspect fit = null;
        double maxFit = 0.0;
        for (Suspect elem : list) {

            if (null != elem && elem.getType().name().equalsIgnoreCase("button")) {
                if (elem.getFitness() > maxFit) {
                    maxFit = elem.getFitness();
                    fit = elem;
                    System.out.println("New fit found!");
                }
            }
        }
        System.out.println(fit);

//        singlePicOCR();
        System.out.println("THE ELEMENT IS IN " + fit.getPath());
    }

    public static Suspect runTorch(String path) throws IOException, InterruptedException {
        String exec = "th classif.lua " + path;
        Suspect elem = null;

        Process proc = Runtime.getRuntime().exec(exec);
        proc.waitFor();
        BufferedReader reader
                = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        String[] parts = null;
        while ((line = reader.readLine()) != null) {
            parts = line.split("\t");
        }
        if (parts != null) {
//            for (String part : parts) {
//                System.out.println(part);
//            }
//            System.out.print("\n");
            if (parts.length == 3) {
                Suspect.ElementType type = Suspect.ElementType.OTHER;
                switch (parts[1]) {
                    case "button":
                        type = Suspect.ElementType.BUTTON;
                        break;
                    case "checkbox":
                        type = Suspect.ElementType.CHECKBOX;
                        break;
                    case "input":
                        type = Suspect.ElementType.INPUT;
                        break;
                    case "other":
                        type = Suspect.ElementType.OTHER;
                        break;
                    default:
                        break;
                }
                elem = new Suspect(path, type, Double.parseDouble(parts[parts.length - 1]));
            }
        }

        proc.waitFor();
        return elem;
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

}
