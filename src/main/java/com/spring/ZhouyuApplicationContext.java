package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZhouyuApplicationContext {

    private  Class configClass;
    //单例池
    private ConcurrentHashMap<String,Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    public ZhouyuApplicationContext(Class configClass) {
        this.configClass = configClass;
        //解析配置类
        //ComponentScan注解--》扫描路径--》扫描--》BeanDefinitionMap
        scan(configClass);
        for(Map.Entry<String,BeanDefinition> entry:beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                //单例bean对象
                Object bean = createBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }

    }
    public Object createBean(String beanName,BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            //依赖注入
            for (Field declaredField: clazz.getDeclaredFields()) {
                if(declaredField.isAnnotationPresent(Autowired.class)){
                    //给某个属性赋值
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    //给某个对象赋值某个属性
                    declaredField.set(instance,bean);
                }
            }
            //Aware回调
            //是否实现了BeanNameAware,实例化一个对象，给这个对象里面的属性赋值
            if(instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }
            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList) {
                //对instance进行额外加工，因此前后可能不是同一个对象
                instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }

            //初始化
            if(instance instanceof InitializingBean){
                try {
                    ((InitializingBean)instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //BeanPostProcessor扩展机制
            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList) {
                //对instance进行额外加工，因此前后可能不是同一个对象
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {

        //判断传过来的类是否有一个注解
        ComponentScan componentScanAnnotation = (ComponentScan)configClass.getDeclaredAnnotation(ComponentScan.class);
        //得到扫描路径
        String path = componentScanAnnotation.value();
        //System.out.println(path);
        path = path.replace(".","/");
        //扫描
        //根据扫描路径找到包下面的所有的类，使用类加载器
        //Bootstrap -->jre/lib
        //Ext-->jre/ext/lib
        //App-->classpath
        //以下得到的是AppClassLoader
        ClassLoader classLoader = ZhouyuApplicationContext.class.getClassLoader();
        //拿到资源，相对的是classpath，例如classLoader.getResource(com/zhouyu/service)
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        //判断是否为一个文件夹
        if(file.isDirectory()){
            File[] files = file.listFiles();
            //遍历所有文件（类）
            for (File f:files) {
                String fileName = f.getAbsolutePath();
                //是否为class文件
                if(fileName.endsWith(".class")){
                    String className = fileName.substring(fileName.indexOf("com"),fileName.indexOf(".class"));
                    //转换成需要加载的类路径
                    className = className.replace("\\",".");
                    try {
                        //加载类，得到class对象
                        Class<?> clazz = classLoader.loadClass(className);
                        //判断每个类中是否存在Component注解
                        if(clazz.isAnnotationPresent(Component.class)){
                            //表示这个类是一个Bean
                            //解析类，判断当前bean是单例bean，还是prototype的bean
                            //发现是一个BeanPostProcessor，clazz对象实现了BeanPostProcessor这个接口
                            if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                                BeanPostProcessor instance = (BeanPostProcessor)clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }

                            //解析类-》BeanDefinition，对bean进行解析
                            Component componentAnnotation = clazz.getAnnotation(Component.class);
                            String beanName = componentAnnotation.value();
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if(clazz.isAnnotationPresent(Scope.class)){
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            }else{
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinitionMap.put(beanName,beanDefinition);
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                //单例bean
                Object o = singletonObjects.get(beanName);
                return o;
            }else{
                //创建bean对象
                Object bean = createBean(beanName,beanDefinition);
                return bean;
            }
        }else {
            //不存在对应的bean
            throw new NullPointerException();
        }
    }
}
