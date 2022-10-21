package ru.deyev.credit.deal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.deal.model.LoanApplicationRequestDTO;
import ru.deyev.credit.deal.model.LoanOfferDTO;
import ru.deyev.credit.deal.model.ScoringDataDTO;
import ru.deyev.credit.deal.service.DealService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @PostMapping("/application")
    public ResponseEntity<List<LoanOfferDTO>> createApplication(@RequestBody LoanApplicationRequestDTO request) {
        return ResponseEntity.ok(dealService.createApplication(request));
    }

    @PutMapping("/offer")
    public ResponseEntity<Void> applyOffer(@RequestBody LoanOfferDTO request) {
        dealService.applyOffer(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping(("/calculate/{applicationId}"))
    public ResponseEntity<Void> calculateCredit(@PathVariable Long applicationId, @RequestBody ScoringDataDTO scoringData) {
        log.info("DealController: calculateCredit() - start, applicationId = {}, scoringData = {}", applicationId, scoringData);
        dealService.calculateCredit(applicationId, scoringData);
        log.info("DealController: calculateCredit() - end, credit information sent to client's email");
        return ResponseEntity.ok().build();
    }
}
