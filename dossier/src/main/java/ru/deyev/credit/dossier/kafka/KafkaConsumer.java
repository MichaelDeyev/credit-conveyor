package ru.deyev.credit.dossier.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.deyev.credit.dossier.feign.DealFeignClient;
import ru.deyev.credit.dossier.model.ApplicationStatus;
import ru.deyev.credit.dossier.model.EmailMessage;
import ru.deyev.credit.dossier.model.MessageFromKafka;
import ru.deyev.credit.dossier.sender.EmailSender;
import ru.deyev.credit.dossier.service.MessageService;
import ru.deyev.credit.dossier.service.PrintedFormService;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class KafkaConsumer {

    private EmailSender emailSender;
    private MessageService messageService;
    private PrintedFormService printedFormService;
    private DealFeignClient dealFeignClient;

    @KafkaListener(topics = "conveyor-finish-registration", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFinishRegistrationMessage(String message) {
        log.info("Consume finish registration message from kafka: {}", message);

        MessageFromKafka messageFromKafka = messageService.parseMessageFromJSON(message);
        log.info("MessageFromKafka = {}", messageFromKafka);

        EmailMessage emailMessage = messageService.kafkaMessageToEmailMessage(messageFromKafka);
        log.info("EmailMessage = {}", emailMessage);

        emailSender.sendMessage(emailMessage.getAddress(), emailMessage.getSubject(), emailMessage.getText());
    }

    @KafkaListener(topics = "conveyor-create-documents", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCreateDocumentsMessage(String message) {
        log.info("Consume create documents message from kafka: {}", message);

        MessageFromKafka messageFromKafka = messageService.parseMessageFromJSON(message);
        log.info("MessageFromKafka = {}", messageFromKafka);

        EmailMessage emailMessage = messageService.kafkaMessageToEmailMessage(messageFromKafka);
        log.info("EmailMessage = {}", emailMessage);

        emailSender.sendMessage(emailMessage.getAddress(), emailMessage.getSubject(), emailMessage.getText());
    }

    @KafkaListener(topics = "conveyor-send-documents", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSendDocumentsMessage(String message) {
        log.info("Consume send documents message from kafka: {}", message);

        MessageFromKafka messageFromKafka = messageService.parseMessageFromJSON(message);
        log.info("MessageFromKafka = {}", messageFromKafka);

        EmailMessage emailMessage = messageService.kafkaMessageToEmailMessage(messageFromKafka);
        log.info("EmailMessage = {}", emailMessage);

        List<File> documents = printedFormService.createDocuments(messageFromKafka.getApplicationId());
        Map<String, File> documentsWithNames = documents.stream()
                .collect(Collectors.toMap(File::getName, file -> file));

        dealFeignClient.updateApplicationStatusById(messageFromKafka.getApplicationId(), ApplicationStatus.DOCUMENT_CREATED.name());

        emailSender.sendMessageWithAttachment(emailMessage.getAddress(),
                emailMessage.getSubject(),
                emailMessage.getText(),
                documentsWithNames);
    }

    @KafkaListener(topics = "conveyor-send-ses", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSendSesMessage(String message) {
        log.info("Consume send ses message from kafka: {}", message);

        MessageFromKafka messageFromKafka = messageService.parseMessageFromJSON(message);
        log.info("MessageFromKafka = {}", messageFromKafka);

        EmailMessage emailMessage = messageService.kafkaMessageToEmailMessage(messageFromKafka);
        log.info("EmailMessage = {}", emailMessage);

        emailSender.sendMessage(emailMessage.getAddress(), emailMessage.getSubject(), emailMessage.getText());
    }

    @KafkaListener(topics = "conveyor-credit-issued", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCreditIssuedMessage(String message) {
        log.info("Consume credit issued message from kafka: {}", message);

        MessageFromKafka messageFromKafka = messageService.parseMessageFromJSON(message);
        log.info("MessageFromKafka = {}", messageFromKafka);

        EmailMessage emailMessage = messageService.kafkaMessageToEmailMessage(messageFromKafka);
        log.info("EmailMessage = {}", emailMessage);

        emailSender.sendMessage(emailMessage.getAddress(), emailMessage.getSubject(), emailMessage.getText());
    }

    @KafkaListener(topics = "conveyor-application-denied", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeApplicationDeniedMessage(String message) {
        log.info("Consume application denied message from kafka: {}", message);

        MessageFromKafka messageFromKafka = messageService.parseMessageFromJSON(message);
        log.info("MessageFromKafka = {}", messageFromKafka);

        EmailMessage emailMessage = messageService.kafkaMessageToEmailMessage(messageFromKafka);
        log.info("EmailMessage = {}", emailMessage);

        emailSender.sendMessage(emailMessage.getAddress(), emailMessage.getSubject(), emailMessage.getText());
    }

}
