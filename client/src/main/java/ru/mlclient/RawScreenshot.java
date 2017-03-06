package ru.mlclient;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author scorpds
 */
public class RawScreenshot {
    
    private String base64;
    private byte[] bytes;
    private File imageFile;

    public RawScreenshot(String screenPath) throws IOException {
        this.imageFile = new File(screenPath);
        bytes = Files.readAllBytes(imageFile.toPath());
    }
    
    public RawScreenshot(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream bArray = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", bArray);
        bytes = bArray.toByteArray();
    }

    public String getBase64() {
        if (base64.isEmpty()) {
            base64 = Base64.encode(bytes);
        }
        return base64;
    }

    public byte[] getBytes() {
        return bytes;
    }
    
    public String toString() {
        return "\"base64\": \"" + base64 + "\"";
    }
    
    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder().add("base64", base64);
        return builder.build();
    }
}
