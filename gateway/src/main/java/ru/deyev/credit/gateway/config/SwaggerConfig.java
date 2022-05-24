package ru.deyev.credit.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${custom.swagger.service.url}")
    String serviceUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Credit Conveyor API")
                        .version("v.1.0.0")
                        .description("Simple Credit Conveyor based on Java, Spring, Microservices, Docker, Kafka and Postgres.")
                        .contact(new Contact()
                                .url("https://github.com/alxdv97")
                                .name("Deyev Alex | Deyev Michael")
                        ))
                .servers(List.of(new Server()
                        .description("localhost")
                        .url(serviceUrl)));
    }
}
