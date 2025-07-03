package io.github.rockcrafters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

@SpringBootApplication
public class Application {

    public Application() {
        Yaml yaml = new Yaml();
        System.out.println("Hello World!" + yaml.load("Hello: World"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
