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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

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
