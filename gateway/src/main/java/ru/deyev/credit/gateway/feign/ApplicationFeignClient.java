package ru.deyev.credit.gateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.deyev.credit.gateway.model.LoanApplicationRequestDTO;
import ru.deyev.credit.gateway.model.LoanOfferDTO;

import java.util.List;

@FeignClient(url = "${custom.feign.url.application}", name = "APPLICATION-FEIGN-CLIENT")
public interface ApplicationFeignClient {

    @PostMapping
    ResponseEntity<List<LoanOfferDTO>> createApplication(@RequestBody LoanApplicationRequestDTO request);

    @PutMapping("/offer")
    ResponseEntity<Void> applyOffer(@RequestBody LoanOfferDTO loanOfferDTO);
}
