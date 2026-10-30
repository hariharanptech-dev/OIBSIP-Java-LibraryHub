package com.libraryhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LibraryHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryHubApplication.class, args);
    }
}
