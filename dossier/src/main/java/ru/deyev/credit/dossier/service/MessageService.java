package ru.deyev.credit.dossier.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.deyev.credit.dossier.feign.DealFeignClient;
import ru.deyev.credit.dossier.model.ApplicationDTO;
import ru.deyev.credit.dossier.model.EmailMessage;
import ru.deyev.credit.dossier.model.MessageFromKafka;
import ru.deyev.credit.dossier.sender.EmailSender;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    @Value("${custom.message.finish-registration.subject}")
    private String FINISH_REGISTRATION_SUBJECT;
    @Value("${custom.message.finish-registration.text}")
    private String FINISH_REGISTRATION_TEXT;
    @Value("${custom.message.create-document.subject}")
    private String CREATE_DOCUMENT_SUBJECT;
    @Value("${custom.message.create-document.text}")
    private String CREATE_DOCUMENT_TEXT;
    @Value("${custom.message.send-document.subject}")
    private String SEND_DOCUMENT_SUBJECT;
    @Value("${custom.message.send-document.text}")
    private String SEND_DOCUMENT_TEXT;
    @Value("${custom.message.send-ses.subject}")
    private String SEND_SES_SUBJECT;
    @Value("${custom.message.send-ses.text}")
    private String SEND_SES_TEXT;
    @Value("${custom.message.credit-issued.subject}")
    private String CREDIT_ISSUED_SUBJECT;
    @Value("${custom.message.credit-issued.text}")
    private String CREDIT_ISSUED_TEXT;
    @Value("${custom.message.application-denied.subject}")
    private String APPLICATION_DENIED_SUBJECT;
    @Value("${custom.message.application-denied.text}")
    private String APPLICATION_DENIED_TEXT;

    private final ObjectMapper objectMapper;

    private final EmailSender emailSender;

    private final DealFeignClient dealFeignClient;

    public MessageFromKafka parseMessageFromJSON(String messageJSON) {
        try {
            log.info("MessageService.parseMessageFromKafka() received message \"{}\"", messageJSON);
            MessageFromKafka messageFromKafka = objectMapper.readValue(messageJSON, MessageFromKafka.class);
            log.info("MessageService.parseMessageFromKafka() parsed message \"{}\"", messageFromKafka);
            return messageFromKafka;
        } catch (JsonProcessingException e) {
            log.warn("Cannot parse messageJSON \"{}\" to JSON", messageJSON);
            throw new RuntimeException(e);
        }
    }

    public EmailMessage kafkaMessageToEmailMessage(MessageFromKafka fromKafka) {
        String subject;
        String text;

        switch (fromKafka.getTheme()) {
            case FINISH_REGISTRATION: {
                ApplicationDTO application = dealFeignClient.getApplicationById(fromKafka.getApplicationId());
                String finishRegistrationTextEvaluated = FINISH_REGISTRATION_TEXT
                        .replaceAll("\\{applicationId\\}", application.getId().toString());
                subject = FINISH_REGISTRATION_SUBJECT;
                text = finishRegistrationTextEvaluated;
                break;
            }
            case CREATE_DOCUMENT: {
                ApplicationDTO application = dealFeignClient.getApplicationById(fromKafka.getApplicationId());
                String createDocumentTextEvaluated = CREATE_DOCUMENT_TEXT
                        .replaceAll("\\{applicationId\\}", application.getId().toString());
                subject = CREATE_DOCUMENT_SUBJECT;
                text = createDocumentTextEvaluated;
                break;
            }
            case SEND_DOCUMENT: {
                ApplicationDTO application = dealFeignClient.getApplicationById(fromKafka.getApplicationId());
                String sendDocumentTextEvaluated = SEND_DOCUMENT_TEXT.replaceAll("\\{applicationId\\}", application.getId().toString());
                subject = SEND_DOCUMENT_SUBJECT;
                text = sendDocumentTextEvaluated;
                break;
            }
            case SEND_SES: {
                ApplicationDTO application = dealFeignClient.getApplicationById(fromKafka.getApplicationId());
                String sesCode = application.getSesCode();
                String sendSesCodeTextEvaluated = SEND_SES_TEXT
                        .replaceAll("\\{sesCode\\}", sesCode)
                        .replaceAll("\\{applicationId\\}", application.getId().toString());
                subject = SEND_SES_SUBJECT;
                text = sendSesCodeTextEvaluated;
                break;
            }
            case CREDIT_ISSUED: {
                ApplicationDTO application = dealFeignClient.getApplicationById(fromKafka.getApplicationId());
                String sendDocumentTextEvaluated = CREDIT_ISSUED_TEXT.replaceAll("\\{applicationId\\}", application.getId().toString());
                subject = CREDIT_ISSUED_SUBJECT;
                text = sendDocumentTextEvaluated;
                break;
            }
            case APPLICATION_DENIED: {
                ApplicationDTO application = dealFeignClient.getApplicationById(fromKafka.getApplicationId());
                String sendDocumentTextEvaluated = APPLICATION_DENIED_TEXT.replaceAll("\\{applicationId\\}", application.getId().toString());
                subject = APPLICATION_DENIED_SUBJECT;
                text = sendDocumentTextEvaluated;
                break;
            }
            default: {
                log.warn("Incorrect messageType \"{}\"", fromKafka.getTheme());
                throw new RuntimeException("Incorrect messageType");
            }
        }

        return new EmailMessage(fromKafka.getAddress(), subject, text);
    }

    public void sendMessage(MessageFromKafka messageFromKafka) {
        log.info("MessageService.sendMessage() send message \"{}\"", messageFromKafka);
        emailSender.sendMessage(kafkaMessageToEmailMessage(messageFromKafka));
    }
}
