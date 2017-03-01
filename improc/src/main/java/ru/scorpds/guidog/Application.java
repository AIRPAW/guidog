package ru.scorpds.guidog;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import org.springframework.boot.builder.SpringApplicationBuilder;
import ru.scorpds.guidog.service.CharactersRecognition;
import ru.scorpds.guidog.service.ImageDecomposition;

/**
 *
 * @author scorpds
 */
//@SpringBootApplication
public class Application {

    public static Properties prop = new Properties();

    public static void main(String[] args) throws IOException, InterruptedException {
//        SpringApplicationBuilder b = new SpringApplicationBuilder(Application.class);
//        b.headless(false).properties("application.properties").run(args); 
        prop.load(ImageDecomposition.class.getClassLoader().getResourceAsStream("application.properties"));
        runMain();
    }

    public static void singlePicOCR() {
        CharactersRecognition.setLang("eng");
        CharactersRecognition.setPic("output/text.png");
        String elementText = CharactersRecognition.getTextFromPic();

        System.out.println(elementText);
    }

    public static void runMain() throws IOException, InterruptedException {

        BufferedImage img = ImageIO.read(new File("grey.png"));

        HashMap<Integer, Point> coords = ImageDecomposition.notMain(img);

        List<ElementCandidate> list = new ArrayList<>();
        for (Integer key : coords.keySet()) {
            list.add(runTorch(key + ".jpg"));
            ElementCandidate tmp = list.get(list.size() - 1);
            if (tmp != null) {
                tmp.setCoords(coords.get(key).x, coords.get(key).y);
            }

        }

        ElementCandidate fit = null;
        double maxFit = 0.0;
        for (ElementCandidate elem : list) {

            if (null != elem && elem.getType().name().equalsIgnoreCase("button")) {
                if (elem.getFitness() > maxFit) {
                    maxFit = elem.getFitness();
                    fit = elem;
                    System.out.println("New fit found!");
                }
            }
        }
        System.out.println(fit);

        singlePicOCR();

        System.out.println("THE ELEMENT IS IN " + fit.getPath());
    }

    public static ElementCandidate runTorch(String path) throws IOException, InterruptedException {
        String exec = "th classif.lua " + path;
        ElementCandidate elem = null;

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
                ElementCandidate.ElementType type = ElementCandidate.ElementType.OTHER;
                switch (parts[1]) {
                    case "button":
                        type = ElementCandidate.ElementType.BUTTON;
                        break;
                    case "checkbox":
                        type = ElementCandidate.ElementType.CHECKBOX;
                        break;
                    case "input":
                        type = ElementCandidate.ElementType.INPUT;
                        break;
                    case "other":
                        type = ElementCandidate.ElementType.OTHER;
                        break;
                    default:
                        break;
                }
                elem = new ElementCandidate(path, type, Double.parseDouble(parts[parts.length - 1]));
            }
        }

        proc.waitFor();
        return elem;
    }
}
