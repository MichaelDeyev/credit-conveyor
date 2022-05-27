package ru.deyev.credit.deal.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.deal.service.DocumentService;

@RestController
@RequestMapping("deal/document")
@AllArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("{applicationId}/send")
    public void sendDocuments(@PathVariable Long applicationId) {
        documentService.sendDocuments(applicationId);
    }

    @PostMapping("{applicationId}/sign")
    public void signDocuments(@PathVariable Long applicationId) {
        documentService.signDocuments(applicationId);
    }

    @PostMapping("{applicationId}/code")
    public void verifyCode(@PathVariable Long applicationId, @RequestBody Integer sesCode) {
        documentService.verifyCode(applicationId, sesCode);
    }
}
