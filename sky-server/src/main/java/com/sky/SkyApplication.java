package com.sky;

import com.sky.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        ApplicationContext context= SpringApplication.run(SkyApplication.class, args);
        AliOssProperties aliOssProperties = context.getBean(AliOssProperties.class);
        //aliOssProperties.printProperties();
        log.info("server started");
    }
}
 