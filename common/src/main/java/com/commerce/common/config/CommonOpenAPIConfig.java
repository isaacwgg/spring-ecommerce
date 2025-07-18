package com.commerce.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonOpenAPIConfig {
  
  @Value("${spring.application.name}")
  private String applicationName;
  
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("bearer-token",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            ))
        .info(new Info()
            .title(formatServiceName(applicationName))
            .version("1.0.0")
            .description(formatServiceName(applicationName) + " API Documentation"))
        .addSecurityItem(new SecurityRequirement().addList("bearer-token"));
  }
  
  private String formatServiceName(String name) {
    return name.substring(0, 1).toUpperCase() +
        name.substring(1).replace("-", " ");
  }
}
