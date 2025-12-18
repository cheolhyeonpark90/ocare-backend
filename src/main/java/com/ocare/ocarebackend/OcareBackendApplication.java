package com.ocare.ocarebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class OcareBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcareBackendApplication.class, args);
    }

}
