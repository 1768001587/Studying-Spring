package com.zhouyu.service;


import com.spring.*;

@Component("userService")
//@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Autowired
    private OrderService orderService;

    private String name;

//    @Override
//    public void setBeanName(String name) {
//        beanName = name;
//    }
    @Override
    public void test(){
        System.out.println(orderService);
        System.out.println(name);
    }

//    @Override
//    public void afterPropertiesSet() throws Exception {
//        System.out.println("初始化");
//    }

    public void setName(String name) {
        this.name = name;
    }
}
