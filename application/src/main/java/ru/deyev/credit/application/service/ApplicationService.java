package ru.deyev.credit.application.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.deyev.credit.application.exception.PreScoringException;
import ru.deyev.credit.application.feign.DealFeignClient;
import ru.deyev.credit.application.model.LoanApplicationRequestDTO;
import ru.deyev.credit.application.model.LoanOfferDTO;
import ru.deyev.credit.application.validator.ApplicationValidator;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ApplicationService {

    private final DealFeignClient dealFeignClient;

    private final ApplicationValidator applicationValidator;

    public List<LoanOfferDTO> createLoanApplication(LoanApplicationRequestDTO request) {
        try {
            applicationValidator.preScoring(request);
        } catch (Exception e) {
            log.warn("Pre-scoring failed by {}", e.getMessage());
            throw new PreScoringException(e.getMessage());
        }
        List<LoanOfferDTO> loanOffers = dealFeignClient.createApplication(request).getBody();
        log.info("Received offers: {}", loanOffers);
        return loanOffers;
    }

    public void applyOffer(LoanOfferDTO loanOfferDTO) {
        log.info("applyOffer() loanOfferDTO = {}", loanOfferDTO);
        dealFeignClient.applyOffer(loanOfferDTO);
    }
}
