package ru.deyev.credit.dossier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.dossier.model.MessageFromKafka;
import ru.deyev.credit.dossier.service.MessageService;

@RestController
@RequestMapping("/dossier/test")
@RequiredArgsConstructor
public class TestController {

    private final MessageService messageService;

    @PostMapping("/message")
    public void sendMessage(@RequestBody MessageFromKafka message) {
        messageService.sendMessage(message);
    }

    @PostMapping("/documents")
    public void sendDocuments(@RequestBody MessageFromKafka message) {
        messageService.sendMessage(message);
    }
}
