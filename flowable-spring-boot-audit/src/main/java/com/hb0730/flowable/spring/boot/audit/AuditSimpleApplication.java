package com.hb0730.flowable.spring.boot.audit;

import com.hb0730.commons.spring.SpringContextUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author bing_huang
 */
@SpringBootApplication
public class AuditSimpleApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(AuditSimpleApplication.class, args);
        SpringContextUtils.setApplicationContext(applicationContext);

    }
}
