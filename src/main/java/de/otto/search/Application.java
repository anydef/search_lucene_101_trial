package de.otto.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is a small example of a Lucene search application that takes product
 * varations for indexing.
 */
@SpringBootApplication(scanBasePackageClasses = Application.class, scanBasePackages = { "de.otto.search" })
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
