package ru.guidog.controller;

import ru.guidog.service.ImageDecomposition;
import ru.guidog.model.Suspect;
import ru.guidog.model.SuspectsList;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author scorpds
 */
@RestController
public class HttpController {

    Logger log = LogManager.getLogger(HttpController.class);

    @RequestMapping(value = "/blob", method = RequestMethod.POST)
    public ResponseEntity<String> processScreenshot(MultipartRequest req, UriComponentsBuilder ub) throws InterruptedException {
        JsonObject response = null;
        try {

            MultipartHttpServletRequest rs = (MultipartHttpServletRequest) req;
            InputStream in = new ByteArrayInputStream(req.getFile("image").getBytes());

            ImageDecomposition decomp = new ImageDecomposition();

            SuspectsList imgList = decomp.batchedProcessing(getImgFromRequest(in));

            Suspect detectedEl = null;
            long start = 0;
            float last = 0.0f, sumSec = 0.0f;
            log.info("Calling Torch for image classification..");
            start = System.nanoTime();
            detectedEl = runTorch(decomp.getStoragePath(), rs.getParameter("elementType"), rs.getParameter("elementText"));
            last = System.nanoTime() - start;
            last = last / 1000000000;
            sumSec += last;
            log.info("All images classification has took " + sumSec + " seconds!");
            for (Suspect s : imgList) {
                if (s.getPath().equals(detectedEl.getPath())) {
                    detectedEl.setX(s.getX());
                    detectedEl.setY(s.getY());
                    continue;
                }
            }

            File dir = new File(decomp.getStoragePath());
            for (File file : dir.listFiles()) {
                file.delete();
            }

            System.out.println(detectedEl);
            response = Json.createObjectBuilder()
                    .add("point",
                            Json.createObjectBuilder()
                                    .add("x", detectedEl.getX())
                                    .add("y", detectedEl.getY())
                                    .build())
                    .build();

            System.out.println("THE ELEMENT IS IN " + detectedEl.getPath());

            for (String string : rs.getParameterMap().keySet()) {
                System.out.println("Request param: " + string + ", value: " + rs.getParameter(string));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.CREATED);
    }

    public Suspect runTorch(String path, String elementType, String elementText) throws IOException, InterruptedException {
        String exec = "th classif.lua " + path + " " + elementType;
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
                elem = new Suspect(parts[parts.length - 1] + ".jpg", type, Double.parseDouble(parts[0]));
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
