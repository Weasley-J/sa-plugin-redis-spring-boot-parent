package com.example.config;

import com.example.AuthenticationInterceptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Authentication Web Mvc Configuration
 */
@Configuration
@ConfigurationProperties(prefix = "sa-token")
@ConditionalOnProperty(prefix = "sa-token", name = {"enable"}, havingValue = "true")
public class AuthWebMvcConfiguration implements WebMvcConfigurer {

    /**
     * 是否启用Sa-Token试拦截器生效
     */
    @Getter
    @Setter
    private Boolean enable;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor()).addPathPatterns("/**");
    }
}
