package ru.deyev.credit.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.deyev.credit.gateway.feign.DealFeignClient;
import ru.deyev.credit.gateway.model.FinishRegistrationRequestDTO;
import ru.deyev.credit.gateway.model.ScoringDataDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {

    private final DealFeignClient dealFeignClient;

    public void finishRegistration(Long applicationId, FinishRegistrationRequestDTO finishRegistrationRequestDTO) {
        ScoringDataDTO scoringDataDTO = new ScoringDataDTO()
                .account(finishRegistrationRequestDTO.getAccount())
                .dependentAmount(finishRegistrationRequestDTO.getDependentAmount())
                .employment(finishRegistrationRequestDTO.getEmployment())
                .gender(ScoringDataDTO.GenderEnum.valueOf(finishRegistrationRequestDTO.getGender().name()))
                .maritalStatus(ScoringDataDTO.MaritalStatusEnum.valueOf(finishRegistrationRequestDTO.getMaritalStatus().name()))
                .passportIssueBranch(finishRegistrationRequestDTO.getPassportIssueBranch())
                .passportIssueDate(finishRegistrationRequestDTO.getPassportIssueDate());
        log.info("finishRegistration(), applicationId = {}, scoringDataDTO = {}", applicationId, scoringDataDTO);
        dealFeignClient.calculateCredit(applicationId, scoringDataDTO);
        log.info("finishRegistration(), credit information sent to client's email");
    }
}
