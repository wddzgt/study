package com.taoge.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.com.taoge.ITestService;
import service.com.taoge.TestService;
import service.com.taoge.TestServiceBack;

@Configuration
@EnableConfigurationProperties(FirstProperties.class)
public class FirstModuleAutoConfiguration {

    @Autowired
    private FirstProperties firstProperties;

    @Bean
    public ITestService createTestService(){
        if(firstProperties.isOpenTestServiceBack()){
            return new TestServiceBack();
        }else{
            return new TestService();
        }
    }

}