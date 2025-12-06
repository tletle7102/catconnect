package com.matchhub.catconnect.global.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * 유효성 검증을 위해 Spring Boot의 기본 Validator 빈을 명시적으로 설정
 * 이미 Spring Boot가 제공하는 LocalValidatorFactoryBean이 자동으로 Bean으로 등록되어 별도 설정은 필요하지 않으나,
 * 명확성을 위해 명시적으로 설정을 추가
 */
@Configuration
public class ValidationConfig {

    /**
     * Spring의 기본 Validator 빈 등록
     * @return LocalValidatorFactoryBean
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}
