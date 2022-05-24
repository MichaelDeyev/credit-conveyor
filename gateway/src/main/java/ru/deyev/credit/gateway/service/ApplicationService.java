package ru.deyev.credit.gateway.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.deyev.credit.gateway.feign.ApplicationFeignClient;
import ru.deyev.credit.gateway.feign.DealFeignClient;
import ru.deyev.credit.gateway.model.ApplicationStatus;
import ru.deyev.credit.gateway.model.LoanApplicationRequestDTO;
import ru.deyev.credit.gateway.model.LoanOfferDTO;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ApplicationService {

    private final ApplicationFeignClient applicationFeignClient;

    private final DealFeignClient dealFeignClient;

    public List<LoanOfferDTO> createLoanApplication(LoanApplicationRequestDTO requestDTO) {
        return applicationFeignClient.createApplication(requestDTO).getBody();
    }

    public void applyOffer(LoanOfferDTO loanOfferDTO) {
        log.info("applyOffer() loanOfferDTO={}", loanOfferDTO);
        applicationFeignClient.applyOffer(loanOfferDTO);
    }

    public void denyLoanApplication(Long applicationId) {
        dealFeignClient.updateApplicationStatusById(applicationId, ApplicationStatus.CLIENT_DENIED.name());
    }

}
