package com.zhouyu;

import com.spring.ZhouyuApplicationContext;
import com.zhouyu.service.UserService;

public class Test {
    public static void main(String[] args) {
        ZhouyuApplicationContext zhouyuApplicationContext =
                new ZhouyuApplicationContext(AppConfig.class);
        //map <beanName,bea n对象>
//        System.out.println(zhouyuApplicationContext.getBean("userService"));
//        System.out.println(zhouyuApplicationContext.getBean("userService"));
//        System.out.println(zhouyuApplicationContext.getBean("userService"));
        UserService userService = (UserService)zhouyuApplicationContext.getBean("userService");
        userService.test();
    }
}
