package com.zhouyu;

import com.spring.ZhouyuApplicationContext;

public class Test {
    public static void main(String[] args) {
        ZhouyuApplicationContext zhouyuApplicationContext =
                new ZhouyuApplicationContext(AppConfig.class);
        //map <beanName,bea n对象>
        System.out.println(zhouyuApplicationContext.getBean("userService"));
        System.out.println(zhouyuApplicationContext.getBean("userService"));
        System.out.println(zhouyuApplicationContext.getBean("userService"));
    }
}
