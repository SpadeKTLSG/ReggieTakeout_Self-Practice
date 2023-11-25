package com.tlsg.takeout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@ServletComponentScan
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching //开启Spring Cache注解方式缓存功能
public class SkTakeOutApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkTakeOutApplication.class, args);
    }

}
