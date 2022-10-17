package ru.deyev.credit.gateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.deyev.credit.gateway.model.AuditAction;

import java.util.List;
import java.util.UUID;

@FeignClient(url = "${custom.feign.url.audit}", name = "AUDIT-FEIGN-CLIENT")
public interface AuditFeignClient {

    @GetMapping("/{auditId}")
    AuditAction getAuditActionById(@PathVariable UUID auditId);

    @GetMapping("/all")
    List<AuditAction> getAllAuditActions();
}
