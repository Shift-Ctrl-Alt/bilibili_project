package com.oymn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BilibiliApplication {

    public static void main(String[] args){
        ApplicationContext app = SpringApplication.run(BilibiliApplication.class, args);

    }
}
