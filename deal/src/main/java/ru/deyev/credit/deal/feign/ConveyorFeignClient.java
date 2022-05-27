package ru.deyev.credit.deal.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.deyev.credit.deal.model.CreditDTO;
import ru.deyev.credit.deal.model.LoanApplicationRequestDTO;
import ru.deyev.credit.deal.model.LoanOfferDTO;
import ru.deyev.credit.deal.model.ScoringDataDTO;

import java.util.List;

@FeignClient(url = "${custom.feign.url.credit-conveyor}", name = "CONVEYOR-FEIGN-CLIENT")
public interface ConveyorFeignClient {

    @PostMapping("/offers")
    ResponseEntity<List<LoanOfferDTO>> generateOffers(@RequestBody LoanApplicationRequestDTO request);

    @PostMapping("/calculation")
    ResponseEntity<CreditDTO> calculateCredit(@RequestBody ScoringDataDTO scoringData);
}
