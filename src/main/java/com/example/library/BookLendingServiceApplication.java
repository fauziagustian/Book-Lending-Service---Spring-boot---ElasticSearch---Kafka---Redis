package com.example.library;

import com.example.library.config.LibraryRulesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LibraryRulesProperties.class)
public class BookLendingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookLendingServiceApplication.class, args);
    }
}
