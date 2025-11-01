package edu.eci.arsw.parcial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Collections;

@SpringBootApplication
public class ParcialApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ParcialApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", getPort()));
        app.run(args);
    }
    static int getPort() {
        String p = System.getenv("PORT");
        if (p != null) return Integer.parseInt(p);
        return 8080;
    }
}
