package com.zhouyu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

@Component
public class ZhouyuBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        if(beanName.equals("userService")){
            System.out.println("初始化前");
            ((UserService)bean).setName("卢开喜是我");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        return bean;
    }
}
