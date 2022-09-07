package ru.deyev.credit.deal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;

import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.requestTo;

@Configuration
public class LogbookConfiguration {

    @Bean
    public Logbook logbook() {
        Logbook logbook = Logbook.builder()
                .condition(exclude(
                        requestTo("/actuator/**"),
                        requestTo("/swagger-ui/**")))
                .build();
        return logbook;
    }
}
