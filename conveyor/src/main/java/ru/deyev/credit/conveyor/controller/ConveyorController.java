package ru.deyev.credit.conveyor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.deyev.credit.conveyor.model.CreditDTO;
import ru.deyev.credit.conveyor.model.LoanApplicationRequestDTO;
import ru.deyev.credit.conveyor.model.LoanOfferDTO;
import ru.deyev.credit.conveyor.model.ScoringDataDTO;
import ru.deyev.credit.conveyor.service.ConveyorFacade;

import java.util.List;

@RestController
@RequestMapping("/conveyor")
@RequiredArgsConstructor
public class ConveyorController {

    private final ConveyorFacade facade;

    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDTO>> generateOffers(@RequestBody LoanApplicationRequestDTO request) {
        return ResponseEntity.ok(facade.generateOffers(request));
    }

    @PostMapping("/calculation")
    public ResponseEntity<CreditDTO> calculateCredit(@RequestBody ScoringDataDTO scoringData) {
        return ResponseEntity.ok(facade.calculateCredit(scoringData));
    }
}
