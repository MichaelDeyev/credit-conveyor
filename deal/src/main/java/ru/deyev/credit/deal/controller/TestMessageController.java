package ru.deyev.credit.deal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.deal.model.EmailMessage;
import ru.deyev.credit.deal.service.DossierService;

@RestController
@RequestMapping("/test/message")
@RequiredArgsConstructor
public class TestMessageController {

    private final DossierService dossierService;

    @PostMapping
    public void sendMessage(@RequestBody EmailMessage emailMessage) {
        dossierService.sendMessage(emailMessage);
    }
}
