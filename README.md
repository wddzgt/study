## springboot
Spring Boot是由Pivotal团队提供的全新框架，其设计目的是用来简化新Spring应用的初始搭建以及开发过程。该框架使用了特定的方式来进行配置，从而使开发人员不再需要定义样板化的配置
## springboot帮我们做了什么
通常搭建一个基于spring的web应用，我们需要做以下工作：

1、pom文件中引入相关jar包，包括spring、springmvc、redis、mybaits、log4j、mysql-connector-java 等等相关jar ...  

2、配置web.xml，Listener配置、Filter配置、Servlet配置、log4j配置、error配置 ...  

3、配置数据库连接、配置spring事务 . 

4、配置视图解析器 . 

5、开启注解、自动扫描功能 . 

6、配置完成后部署tomcat、启动调试 . 

……
而现在，我们可以使用很简单的步骤就可以搭建出来一个web项目架子,使用starter集成的组件，只需要

1、导入相关jar . 

2、配置properties文件 . 

## Spring Boot特性
1、约定优于配置，大多数的配置直接使用默认配置即可 . 

2、快速搭建项目，脱离繁杂的XML配置 . 

3、内嵌Servlet容器，不依赖外部容器 . 

4、与云计算天然集成，提供主流框架一键集成方式 . 

……

spring-boot-starter-data-redis 
## 版本差异
spring boot框架中已经集成了redis，在1.x.x的版本时默认使用的jedis客户端，现在是2.x.x版本默认使用的lettuce客户端，两种客户端的区别如下
-  Jedis和Lettuce都是Redis Client
-  Jedis 是直连模式，在多个线程间共享一个 Jedis 实例时是线程不安全的，
-  如果想要在多线程环境下使用 Jedis，需要使用连接池  
-  每个线程都去拿自己的Jedis实例，当连接数量增多时，物理连接成本就较高了。  
-  Lettuce的连接是基于Netty的，连接实例可以在多个线程间共享，所以，一个多线程的应用可以使用同一个连接实例，而不用担心并发线程的数量。  
-  当然这个也是可伸缩的设计，一个连接实例不够的情况也可以按需增加连接实例。  
- 通过异步的方式可以让我们更好的利用系统资源，而不用浪费线程等待网络或磁盘I/O。 
-  Lettuce 是基于 netty 的，netty 是一个多线程、事件驱动的 I/O 框架，所以 Lettuce可以帮助我们充分利用异步的优势。

## 自动配置
所有的Spring Boot项目都是由SpringApplication.run()这个静态方法开始执行的，在这里进行断点源码可以发现
SpringFactoriesLoader.loadFactoryNames()这个方法是解析properties的核心方法
```
public static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

public static List<String> loadFactoryNames(Class<?> factoryClass, ClassLoader classLoader) {
    String factoryClassName = factoryClass.getName();
    try {
        Enumeration<URL> urls = (classLoader != null ? classLoader.getResources(FACTORIES_RESOURCE_LOCATION) :
                ClassLoader.getSystemResources(FACTORIES_RESOURCE_LOCATION));
        List<String> result = new ArrayList<String>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            Properties properties = PropertiesLoaderUtils.loadProperties(new UrlResource(url));
            String factoryClassNames = properties.getProperty(factoryClassName);
            result.addAll(Arrays.asList(StringUtils.commaDelimitedListToStringArray(factoryClassNames)));
        }
        return result;
    }
    catch (IOException ex) {
        throw new IllegalArgumentException("Unable to load [" + factoryClass.getName() +
                "] factories from location [" + FACTORIES_RESOURCE_LOCATION + "]", ex);
    }
}
```

可以看到实际上是加载META-INF/spring.factories文件夹下的内容，读取到的内容放在Properties中，通过类的名称去获取对应的值。值就是实现类的名称，然后再调用createSpringFactoriesInstances创建相关类的实例，这样就完成了对ApplicationContextInitializer对象的实例化工作 . 

spring.factories这个文件在spring-boot-autoconfigure包下，这种实现方式是类似于Java的SPI扩展机制(Service Provider Interface),
简单介绍一下,我们系统里抽象的各个模块，往往有很多不同的实现方案，比如日志模块的方案，xml解析模块、jdbc模块的方案等。面向的对象的设计里，我们一般推荐模块之间基于接口编程，模块之间不对实现类进行硬编码。一旦代码里涉及具体的实现类，就违反了可拔插的原则，如果需要替换一种实现，就需要修改代码。为了实现在模块装配的时候能不在程序里动态指明，这就需要一种服务发现机制。
为某个接口寻找服务实现的机制，有点类似IOC的思想，就是将装配的控制权移到程序之外,灵活的实现抽象模块的可插拔。  

进入spring.factories文件，找到redis对应的配置类
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
…
org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,\
...
```
打开RedisAutoConfiguration
![image](/uploads/4a4a34c9de95ee15b1440ef43a08ce61/image.png)
先看@EnableConfigurationProperties注解，这个注解的作用使使用 @ConfigurationProperties 注解的类生效。
接着看RedisProperties类
```
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

   /**
    * Database index used by the connection factory.
    */
   private int database = 0;

   /**
    * Connection URL. Overrides host, port, and password. User is ignored. Example:
    * redis://user:password@example.com:6379
    */
   private String url;

   /**
    * Redis server host.
    */
   private String host = "localhost";
```
从这里可以看出，这个properties类读取了前缀为spring.redis的配置，并且给配置属性赋了初始默认值
再回到@Import({ LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class })这里,导入了两种客户端的配置
都看一下
```
@Configuration
@ConditionalOnClass({ GenericObjectPool.class, JedisConnection.class, Jedis.class })
class JedisConnectionConfiguration extends RedisConnectionConfiguration 
 ```
```
@Configuration
@ConditionalOnClass(RedisClient.class)
class LettuceConnectionConfiguration extends RedisConnectionConfiguration
```
@ConditionalOnClass会检查类加载器中是否存在对应的类，如果有的话被注解修饰的类就有资格被Spring容器所注册，否则会被skip
@ConditionalOnMissingClass 与上一个注解相反,类加载器中不存在指明的类会被注册 . 

@ConditionalOnBean，仅在当前上下文中存在某个bean时，才会实例化这个Bean。  

@ConditionalOnExpression，当表达式为true的时候，才会实例化这个Bean。  

@ConditionalOnMissingBean，仅在当前上下文中不存在某个bean时，才会实例化这个Bean。  

@ConditionalOnNotWebApplication，不是web应用时才会实例化这个Bean。  

@AutoConfigureAfter，在某个bean完成自动配置后实例化这个bean。 

@AutoConfigureBefore，在某个bean完成自动配置前实例化这个bean。  

通过@ConditionalOnClass注解来确定使用的是哪一个客户端
 
 
 
 官方文档说明springboot 2.x之后默认使用的是Lettuce客户端连接redis,根据maven依赖也可以看出来,spring-boot-starter-data-redis依赖了lettuce-core 

![image](/uploads/052b7b0d8c1566f53874828d82c628b2/image.png)

进入LettuceConnectionConfiguration类
![image](/uploads/14b21fe6778edcf52fac72a51e23280d/image.png)
到这里，一个lettuce的connectFactory就加载完成了
回到第一张图,可以看到我们使用的redistemplate set 了connectFactory，然后被注入到容器
 ![image](/uploads/58c37841daa0a3939b04d281858b46e4/image.png)

spring-boot-starter 自动配置总结:
1.启动通过SpringFactoriesLoader加载所有classpath下所有JAR文件里面的META-INF/spring.factories文件。  

2.通过spring.factories到对应的auto配置类,读取对应前缀的properties属性，并设置到所需要的类中。  

3.bean初始化注入容器，使用者可以直接获取使用。  


自定义starter demo代码：https://github.com/wddzgt/study
