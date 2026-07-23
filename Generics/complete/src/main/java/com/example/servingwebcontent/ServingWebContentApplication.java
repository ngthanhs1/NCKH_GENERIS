package com.example.servingwebcontent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServingWebContentApplication {

    private static final Logger log = LoggerFactory.getLogger(ServingWebContentApplication.class);

    /**
     * Entry point của application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ServingWebContentApplication.class, args);
    }

    @Bean
    public CommandLineRunner printServerUrl() {
        return args -> {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("  >>> ỨNG DỤNG ĐÃ KHỞI CHẠY THÀNH CÔNG! <<<");
            System.out.println("  >>> TRUY CẬP GIAO DIỆN WEB TẠI: http://localhost:8080/ <<<");
            System.out.println("  >>> Trang Insights:             http://localhost:8080/insights.html <<<");
            System.out.println("  >>> Trang Import/Export:        http://localhost:8080/export.html <<<");
            System.out.println("=".repeat(70) + "\n");
        };
    }
}

