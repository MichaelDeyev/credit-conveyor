package ru.deyev.credit.gateway.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.deyev.credit.gateway.feign.DealFeignClient;

@Service
@AllArgsConstructor
public class DocumentService {

    private final DealFeignClient dealFeignClient;

    public void createDocuments(Long applicationId) {
        dealFeignClient.sendDocuments(applicationId);
    }

    public void sendSesCode(Long applicationId, Integer sesCode) {
        dealFeignClient.verifyCode(applicationId, sesCode);
    }

    public void signDocuments(Long applicationId) {
        dealFeignClient.signDocuments(applicationId);
    }
}
