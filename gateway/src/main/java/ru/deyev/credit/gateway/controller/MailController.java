package ru.deyev.credit.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.gateway.api.EmailApi;
import ru.deyev.credit.gateway.model.MockMailDTO;

@RestController
public class MailController implements EmailApi {

    @Override
    @CrossOrigin(origins = "*")
    public ResponseEntity<Void> mailMock(MockMailDTO mockMailDTO) {
        return ResponseEntity.ok().build();
    }
}
