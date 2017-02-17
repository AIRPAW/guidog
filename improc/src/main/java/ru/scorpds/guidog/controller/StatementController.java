package ru.scorpds.guidog.controller;

import ru.scorpds.guidog.service.ImageDecomposition;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class StatementController {

    @RequestMapping(value = "/blob", method = RequestMethod.POST)
    public ResponseEntity<Void> insertStatementIncremetally(MultipartRequest req, UriComponentsBuilder ub) {
        try {
            MultipartHttpServletRequest rs = (MultipartHttpServletRequest) req;
            InputStream in = new ByteArrayInputStream(req.getFile("image").getBytes());
            BufferedImage bImageFromConvert = ImageIO.read(in);
            String imgPath = "target\\sourceImage.png";
            System.out.println("Saving initial image..");
            ImageIO.write(bImageFromConvert, "png", new File(imgPath));
            ImageDecomposition.notMain(imgPath);
            
            for (String string : rs.getParameterMap().keySet()) {
                System.out.println(string);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }
}
