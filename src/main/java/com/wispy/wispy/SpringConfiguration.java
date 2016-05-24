package com.wispy.wispy;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

/**
 * @author Leonid_Poliakov
 */
@Configuration
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan("com.wispy.wispy")
public class SpringConfiguration {
    @Bean
    public PropertyPlaceholderConfigurer propertyConfigurer() throws IOException {
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setLocations(new ClassPathResource("application.properties"));
        return configurer;
    }
}