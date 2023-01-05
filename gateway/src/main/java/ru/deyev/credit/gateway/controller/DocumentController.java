package ru.deyev.credit.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.gateway.api.DocumentApi;
import ru.deyev.credit.gateway.service.DocumentService;

@RestController
@RequiredArgsConstructor
public class DocumentController implements DocumentApi {

    private final DocumentService documentService;

    @CrossOrigin(origins = "*")
    @Override
    public ResponseEntity<Void> createDocuments(Long applicationId) {
        documentService.createDocuments(applicationId);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = "*")
    @Override
    public ResponseEntity<Void> signDocuments(Long applicationId) {
        documentService.signDocuments(applicationId);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = "*")
    @Override
    public ResponseEntity<Void> sendSesCode(Long applicationId, Integer body) {
        documentService.sendSesCode(applicationId, body);
        return ResponseEntity.ok().build();
    }


}
