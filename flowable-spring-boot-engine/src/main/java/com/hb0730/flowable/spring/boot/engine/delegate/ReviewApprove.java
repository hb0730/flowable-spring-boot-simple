package com.hb0730.flowable.spring.boot.engine.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * 批准委托类
 *
 * @author bing_huang
 */
@Slf4j
public class ReviewApprove implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        //可以发送消息给某人
        log.info("通过，userId是：{}",execution.getVariable("userId"));
    }
}
