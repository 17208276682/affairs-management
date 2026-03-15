package com.affairs.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.affairs.management.mapper")
public class AffairsManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(AffairsManagementApplication.class, args);
    }
}
