package com.hospital.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置
 * 声明 JWT Bearer 认证方式，便于在 Knife4j / Swagger UI 中先登录再调试受保护接口
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerToken";

    @Bean
    public OpenAPI hospitalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("医院预约挂号系统 API")
                        .description("登录接口获取 token 后，点击右上角 Authorize 填入即可调试需要认证的接口")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
