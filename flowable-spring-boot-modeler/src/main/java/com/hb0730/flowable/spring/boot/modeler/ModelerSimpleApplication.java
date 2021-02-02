package com.hb0730.flowable.spring.boot.modeler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author bing_huang
 */
//@Import({
//        ApplicationConfiguration.class,
//        AppDispatcherServletConfiguration.class,
//        ModelerDatabaseConfiguration.class
//})
//@SpringBootApplication(exclude = {
//        SecurityAutoConfiguration.class,
//        SecurityFilterAutoConfiguration.class})
@SpringBootApplication
public class ModelerSimpleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelerSimpleApplication.class, args);
    }
}
