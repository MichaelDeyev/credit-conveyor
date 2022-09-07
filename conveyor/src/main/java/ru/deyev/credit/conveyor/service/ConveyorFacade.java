package ru.deyev.credit.conveyor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.deyev.credit.conveyor.model.CreditDTO;
import ru.deyev.credit.conveyor.model.LoanApplicationRequestDTO;
import ru.deyev.credit.conveyor.model.LoanOfferDTO;
import ru.deyev.credit.conveyor.model.ScoringDataDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConveyorFacade {

    private final OfferService offerService;

    private final ScoringService scoringService;

    public List<LoanOfferDTO> generateOffers(LoanApplicationRequestDTO request) {
        return offerService.generateOffers(request);
    }

    public CreditDTO calculateCredit(ScoringDataDTO scoringData) {
        return scoringService.calculateCredit(scoringData);
    }

}
