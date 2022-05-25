package ru.deyev.credit.application.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.deyev.credit.application.model.LoanApplicationRequestDTO;
import ru.deyev.credit.application.model.LoanOfferDTO;

import java.util.List;

@FeignClient(url = "${custom.feign.url.deal}", name = "DEAL-FEIGN-CLIENT")
public interface DealFeignClient {

    @PostMapping("/application")
    ResponseEntity<List<LoanOfferDTO>> createApplication(@RequestBody LoanApplicationRequestDTO request);

    @PutMapping("/offer")
    ResponseEntity<Void> applyOffer(@RequestBody LoanOfferDTO request);
}
