package com.hb0730.flowable.spring.boot.audit;

import com.hb0730.commons.spring.SpringContextUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author bing_huang
 */
@RunWith(value = SpringRunner.class)
@SpringBootTest(classes = AuditSimpleApplication.class)
public class BaseTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void before() {
        SpringContextUtils.setApplicationContext(applicationContext);
    }
}
