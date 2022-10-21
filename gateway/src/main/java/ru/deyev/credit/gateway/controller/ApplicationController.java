package ru.deyev.credit.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.gateway.api.ApplicationApi;
import ru.deyev.credit.gateway.model.FinishRegistrationRequestDTO;
import ru.deyev.credit.gateway.model.LoanApplicationRequestDTO;
import ru.deyev.credit.gateway.model.LoanOfferDTO;
import ru.deyev.credit.gateway.service.ApplicationService;
import ru.deyev.credit.gateway.service.DealService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApplicationController implements ApplicationApi {

    private final ApplicationService applicationService;

    private final DealService dealService;

    @Override
    public ResponseEntity<Void> applyOffer(LoanOfferDTO loanOfferDTO) {
        applicationService.applyOffer(loanOfferDTO);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<LoanOfferDTO>> createLoanApplication(LoanApplicationRequestDTO loanApplicationRequestDTO) {
        return ResponseEntity.ok(applicationService.createLoanApplication(loanApplicationRequestDTO));
    }

    @Override
    public ResponseEntity<Void> denyLoanApplication(@PathVariable Long applicationId) {
        applicationService.denyLoanApplication(applicationId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> finishRegistration(Long applicationId, FinishRegistrationRequestDTO finishRegistrationRequestDTO) {
        dealService.finishRegistration(applicationId, finishRegistrationRequestDTO);
        return ResponseEntity.ok().build();
    }
}
