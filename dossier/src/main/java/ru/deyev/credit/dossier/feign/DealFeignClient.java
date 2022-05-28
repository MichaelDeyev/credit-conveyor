package ru.deyev.credit.dossier.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.deyev.credit.dossier.model.ApplicationDTO;

@FeignClient(url = "${custom.feign.url.deal}", name = "DEAL-FEIGN-CLIENT")
public interface DealFeignClient {

    @GetMapping("/admin/application/{applicationId}")
    ApplicationDTO getApplicationById(@PathVariable Long applicationId);

    @PutMapping("/admin/application/{applicationId}/status")
    void updateApplicationStatusById(@PathVariable Long applicationId, @RequestParam String statusName);

}
