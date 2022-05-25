package ru.deyev.credit.application.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.deyev.credit.application.exception.PreScoringException;
import ru.deyev.credit.application.model.LoanApplicationRequestDTO;
import ru.deyev.credit.application.model.LoanOfferDTO;
import ru.deyev.credit.application.service.ApplicationService;

import java.util.List;

@RestController
@RequestMapping("/application")
@AllArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<List<LoanOfferDTO>> createApplication(@RequestBody LoanApplicationRequestDTO request) {
        try {
            return ResponseEntity.ok(applicationService.createLoanApplication(request));
        } catch (PreScoringException e) {
            throw new IllegalArgumentException("Pre-scoring failed because: " + e.getMessage());
        }
    }

    @PutMapping("/offer")
    public ResponseEntity<Void> applyOffer(@RequestBody LoanOfferDTO loanOfferDTO) {
        applicationService.applyOffer(loanOfferDTO);
        return ResponseEntity.ok().build();
    }
}
