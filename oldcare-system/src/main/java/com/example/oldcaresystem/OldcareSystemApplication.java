package com.example.oldcaresystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.oldcaresystem.mapper")
public class OldcareSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(OldcareSystemApplication.class, args);
    }

}
