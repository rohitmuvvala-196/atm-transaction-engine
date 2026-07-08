package com.atmengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ATM Transaction Engine API")
                        .description("A production-grade ATM Transaction Engine with robust exception handling, " +
                                "transaction safety, hardware simulation, and comprehensive audit logging.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ATM Engine Team")
                                .email("support@atmengine.com")
                                .url("https://atmengine.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://api.atmengine.com").description("Production Server")
                ));
    }
}