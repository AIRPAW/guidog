package ru.guidog.controller;

import java.awt.Point;
import ru.guidog.service.ImageDecomposition;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.UriComponentsBuilder;
import static ru.guidog.Guide.runTorch;
import ru.guidog.model.Suspect;
import ru.guidog.model.SuspectsList;

/**
 *
 * @author scorpds
 */
@RestController
public class HttpController {

    @RequestMapping(value = "/blob", method = RequestMethod.POST)
    public ResponseEntity<String> processScreenshot(MultipartRequest req, UriComponentsBuilder ub) throws InterruptedException {
        JsonObject response = null;
        try {

            MultipartHttpServletRequest rs = (MultipartHttpServletRequest) req;
            InputStream in = new ByteArrayInputStream(req.getFile("image").getBytes());
            
            ImageDecomposition decomp = new ImageDecomposition();

            SuspectsList imgList = decomp.batchedProcessing(getImgFromRequest(in));

            SuspectsList list = new SuspectsList();
            for (Suspect suspect : imgList) {
                list.add(runTorch(suspect.getPath()));
                list.getLast().setCurImg(suspect.getCurImg());
                list.getLast().setCoords(suspect.getX(), suspect.getY());
            }
            
            File dir = new File(decomp.getStoragePath());
            for (File file : dir.listFiles()) {
                file.delete();
            }

            Suspect fit = null;
            double maxFit = 0.0;
            for (Suspect elem : list) {

                if (null != elem && elem.getType().name().equalsIgnoreCase(rs.getParameter("elementType"))) {                    
                    if (elem.getFitness() > maxFit) {
                        maxFit = elem.getFitness();
                        fit = elem;
                        System.out.println("New fit found!");
                    }
                }
            }
            System.out.println(fit);
            response = Json.createObjectBuilder()
                    .add("point",
                            Json.createObjectBuilder()
                                    .add("x", fit.getX())
                                    .add("y", fit.getY())
                                    .build())
                    .build();

            System.out.println("THE ELEMENT IS IN " + fit.getPath());

            for (String string : rs.getParameterMap().keySet()) {
                System.out.println("Request param: " + string + ", value: " + rs.getParameter(string));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.CREATED);
    }

    public Suspect runTorch(String path) throws IOException, InterruptedException {
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
    
    public Suspect runTorch(Suspect test) throws IOException {
        Suspect susp = null;
        ProcessBuilder pb = new ProcessBuilder();
        return susp;
    }

    private BufferedImage getImgFromRequest(InputStream in) throws IOException {

        BufferedImage bImageFromConvert = ImageIO.read(in);
                
        String imgPath = "sourceImage.png";
        System.out.println("Saving initial image..");
        ImageIO.write(bImageFromConvert, "png", new File(imgPath));

        return bImageFromConvert;
    }
}
