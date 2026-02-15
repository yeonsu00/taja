package com.taja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class TajaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TajaApplication.class, args);
    }

}
