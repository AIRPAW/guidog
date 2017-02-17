package ru.scorpds.guidog;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 *
 * @author scorp
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplicationBuilder b = new SpringApplicationBuilder(Application.class);
        b.headless(false).properties("application.properties").run(args);
    }
}
