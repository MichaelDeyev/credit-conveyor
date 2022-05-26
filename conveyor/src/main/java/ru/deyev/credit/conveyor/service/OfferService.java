package ru.deyev.credit.conveyor.service;

import org.springframework.stereotype.Service;
import ru.deyev.credit.conveyor.model.LoanApplicationRequestDTO;
import ru.deyev.credit.conveyor.model.LoanOfferDTO;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OfferService {

    private final ScoringService scoringService;

    public OfferService(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    public List<LoanOfferDTO> generateOffers(LoanApplicationRequestDTO request) {
        return List.of(
                createOffer(false, false, request),
                createOffer(true, false, request),
                createOffer(false, true, request),
                createOffer(true, true, request)
        );
    }

    private LoanOfferDTO createOffer(Boolean isInsuranceEnabled,
                                     Boolean isSalaryClient,
                                     LoanApplicationRequestDTO request) {

        BigDecimal totalAmount = scoringService.evaluateTotalAmountByServices(request.getAmount(),
                isInsuranceEnabled);

        BigDecimal rate = scoringService.calculateRate(isInsuranceEnabled, isSalaryClient);

        return new LoanOfferDTO()
                .requestedAmount(request.getAmount())
                .totalAmount(totalAmount)
                .term(request.getTerm())
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient)
                .rate(rate)
                .monthlyPayment(scoringService.calculateMonthlyPayment(totalAmount, request.getTerm(), rate));
    }
}
