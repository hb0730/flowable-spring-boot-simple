package com.hb0730.flowable.spring.boot.audit.utils;

import com.hb0730.commons.spring.SpringContextUtils;

/**
 * @author bing_huang
 */
public class BaseUtils {

    public static <T> T getService(Class<T> clazz) {
        return SpringContextUtils.getBean(clazz);
    }
}
