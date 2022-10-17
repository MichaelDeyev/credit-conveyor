package ru.deyev.credit.gateway.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.deyev.credit.gateway.feign.AuditFeignClient;
import ru.deyev.credit.gateway.feign.DealFeignClient;
import ru.deyev.credit.gateway.model.ApplicationDTO;
import ru.deyev.credit.gateway.model.AuditAction;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AdminService {

    private final DealFeignClient dealFeignClient;
    private final AuditFeignClient auditFeignClient;

    public ApplicationDTO getApplicationById(Long applicationId) {
        return dealFeignClient.getApplicationById(applicationId);
    }

    public List<ApplicationDTO> getAllApplications() {
        return dealFeignClient.getAllApplications();
    }

    public AuditAction getAuditActionById(UUID auditId) {
        return auditFeignClient.getAuditActionById(auditId);
    }

    public List<AuditAction> getAllAuditActions() {
        return auditFeignClient.getAllAuditActions();
    }
}
