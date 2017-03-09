package ru.guidog;

import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 *
 * @author scorpds
 */
@SpringBootApplication
public class Guide {
    
    private static Logger log = LogManager.getLogger(Guide.class);

    private static final Properties CONFIG = new Properties();
    private static boolean writeImgOnDisk;
    private static boolean showImagesFrames;;

    static {
        try {
            CONFIG.load(Guide.class.getClassLoader().getResourceAsStream("application.properties"));
            writeImgOnDisk = Guide.CONFIG.getProperty("saveImagesOnDisk").equalsIgnoreCase("true");
            showImagesFrames = Guide.CONFIG.getProperty("showImages").equalsIgnoreCase("true");
            
        } catch (IOException ex) {
            log.log(Level.ERROR,ex);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplicationBuilder b = new SpringApplicationBuilder(Guide.class);
        b.headless(false).properties().run(args);

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
