package com.appsdeveloperblog.app.ws;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringApplicationContext implements ApplicationContextAware {

    private static ApplicationContext appContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext=applicationContext;
    }

    public static Object getBean(String beanName){
       return appContext.getBean(beanName);
    }
}