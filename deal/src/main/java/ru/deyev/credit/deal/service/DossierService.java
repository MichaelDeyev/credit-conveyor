package ru.deyev.credit.deal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.deyev.credit.deal.model.EmailMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class DossierService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${custom.message.topic.finish-registration}")
    private String FINISH_REGISTRATION_TOPIC;

    @Value("${custom.message.topic.create-documents}")
    private String CREATE_DOCUMENT_TOPIC;

    @Value("${custom.message.topic.send-documents}")
    private String SEND_DOCUMENT_TOPIC;

    @Value("${custom.message.topic.send-ses}")
    private String SEND_SES_TOPIC;

    @Value("${custom.message.topic.credit-issued}")
    private String CREDIT_ISSUED_TOPIC;

    @Value("${custom.message.topic.application-denied}")
    private String APPLICATION_DENIED_TOPIC;

    public void sendMessage(EmailMessage message) {
        String topic = evaluateTopic(message.getTheme());
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.warn("Cannot map message \"{}\" to JSON", message);
            throw new RuntimeException(e);
        }

        log.info("Sending message \"{}\" to kafka topic \"{}\"", jsonMessage, topic);
        kafkaTemplate.send(topic, jsonMessage);
    }

    private String evaluateTopic(EmailMessage.ThemeEnum theme) {
        String topic = null;

        switch (theme) {
            case FINISH_REGISTRATION: {
                topic = FINISH_REGISTRATION_TOPIC;
                break;
            }
            case CREATE_DOCUMENT: {
                topic = CREATE_DOCUMENT_TOPIC;
                break;
            }
            case SEND_DOCUMENT: {
                topic = SEND_DOCUMENT_TOPIC;
                break;
            }
            case SEND_SES: {
                topic = SEND_SES_TOPIC;
                break;
            }
            case CREDIT_ISSUED: {
                topic = CREDIT_ISSUED_TOPIC;
                break;
            }
            case APPLICATION_DENIED: {
                topic = APPLICATION_DENIED_TOPIC;
                break;
            }
        }
        return topic;
    }


}
