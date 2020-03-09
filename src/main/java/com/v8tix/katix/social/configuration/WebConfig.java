package com.v8tix.katix.social.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@Configuration
public class WebConfig {

  @Value("${rest.host}")
  private String host;

  @Bean
  public WebClient setUpWebClient() {
    return WebClient.create(host);
  }

  @Bean
  @Qualifier("JSR-380-Validator")
  public Validator setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    return factory.getValidator();
  }
}
