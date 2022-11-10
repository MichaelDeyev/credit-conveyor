package ru.deyev.credit.deal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.deyev.credit.deal.feign.ConveyorFeignClient;
import ru.deyev.credit.deal.metric.MeasureService;
import ru.deyev.credit.deal.metric.Monitored;
import ru.deyev.credit.deal.model.Application;
import ru.deyev.credit.deal.model.ApplicationStatus;
import ru.deyev.credit.deal.model.ApplicationStatusHistoryDTO;
import ru.deyev.credit.deal.model.Client;
import ru.deyev.credit.deal.model.Credit;
import ru.deyev.credit.deal.model.CreditDTO;
import ru.deyev.credit.deal.model.EmailMessage;
import ru.deyev.credit.deal.model.LoanApplicationRequestDTO;
import ru.deyev.credit.deal.model.LoanOfferDTO;
import ru.deyev.credit.deal.model.PassportInfo;
import ru.deyev.credit.deal.model.ScoringDataDTO;
import ru.deyev.credit.deal.repository.ApplicationRepository;
import ru.deyev.credit.deal.repository.ClientRepository;
import ru.deyev.credit.deal.repository.CreditRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static ru.deyev.credit.deal.model.ApplicationStatus.APPROVED;
import static ru.deyev.credit.deal.model.ApplicationStatus.CC_APPROVED;
import static ru.deyev.credit.deal.model.ApplicationStatus.CC_DENIED;
import static ru.deyev.credit.deal.model.ApplicationStatus.PREAPPROVAL;
import static ru.deyev.credit.deal.model.ApplicationStatusHistoryDTO.ChangeTypeEnum.AUTOMATIC;
import static ru.deyev.credit.deal.model.ApplicationStatusHistoryDTO.ChangeTypeEnum.MANUAL;
import static ru.deyev.credit.deal.model.CreditStatus.CALCULATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService {

    private final ConveyorFeignClient conveyorFeignClient;

    private final ApplicationRepository applicationRepository;

    private final ClientRepository clientRepository;

    private final DocumentService documentService;

    private final DossierService dossierService;

    private final CreditRepository creditRepository;

    private final AdminService adminService;

    public List<LoanOfferDTO> createApplication(@RequestBody LoanApplicationRequestDTO request) {
        Client newClient = createClientByRequest(request);

        Client savedClient = clientRepository.save(newClient);

        log.info("createApplication(), savedClient={}", savedClient);
        Application newApplication = new Application()
                .setClient(savedClient)
                .setCreationDate(LocalDate.now());

        adminService.updateApplicationStatus(newApplication, PREAPPROVAL, MANUAL);

        Application savedApplication = applicationRepository.save(newApplication);
        clientRepository.save(savedClient.setApplication(savedApplication));

        List<LoanOfferDTO> loanOffers = conveyorFeignClient.generateOffers(request).getBody();

        //        TODO
//        measureService.incrementStatusCounter(savedApplication.getStatus());

        if (loanOffers != null) {
            loanOffers.forEach(loanOfferDTO -> loanOfferDTO.setApplicationId(savedApplication.getId()));
        }

        log.info("createApplication(), savedApplication={}", savedApplication);
        log.info("Received offers: {}", loanOffers);
        return loanOffers;
    }

    public void calculateCredit(Long applicationId, ScoringDataDTO scoringData) {
        Application application = applicationRepository.findById(applicationId).orElseThrow(EntityNotFoundException::new);
        Client client = application.getClient();
        LoanOfferDTO appliedOffer = application.getAppliedOffer();
        scoringData
                .amount(appliedOffer.getTotalAmount())
                .term(appliedOffer.getTerm())
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .lastName(client.getLastName())
                .birthdate(client.getBirthDate())
                .passportSeries(client.getPassportInfo().getSeries())
                .passportNumber(client.getPassportInfo().getNumber())
                .isInsuranceEnabled(appliedOffer.getIsInsuranceEnabled())
                .isSalaryClient(appliedOffer.getIsSalaryClient());

        log.info("calculateCredit(), full scoringData={}", scoringData);
        CreditDTO creditDTO;

        PassportInfo fullPassportInfo = new PassportInfo()
                .series(client.getPassportInfo().getSeries())
                .number(client.getPassportInfo().getNumber())
                .issueDate(scoringData.getPassportIssueDate())
                .issueBranch(scoringData.getPassportIssueBranch());


        try {
            creditDTO = conveyorFeignClient.calculateCredit(scoringData).getBody();
        } catch (Exception e) {
            log.warn("Credit conveyor denied application by these reasons: {}", e.getMessage());

            denyApplication(application, client, scoringData, fullPassportInfo, AUTOMATIC);

//           method ends normally, clients will know about deny by email
            return;
        }
        log.info("calculateCredit(), credit after calculating creditDTO={}", creditDTO);

        assert Objects.nonNull(creditDTO);
        Credit credit = creditRepository.save(new Credit()
                .setAmount(creditDTO.getAmount())
                .setApplication(application)
                .setClient(client)
                .setIsInsuranceEnabled(creditDTO.getIsInsuranceEnabled())
                .setMonthlyPayment(creditDTO.getMonthlyPayment())
                .setIsSalaryClient(creditDTO.getIsSalaryClient())
                .setPaymentSchedule(creditDTO.getPaymentSchedule())
                .setPsk(creditDTO.getPsk())
                .setRate(creditDTO.getRate())
                .setTerm(creditDTO.getTerm())
                .setCreditStatus(CALCULATED));
        log.info("calculateCredit(), saved credit={}", credit);

        clientRepository.save(client
                .setGender(scoringData.getGender().name())
                .setPassportInfo(fullPassportInfo)
                .setMaritalStatus(scoringData.getMaritalStatus().name())
                .setDependentAmount(scoringData.getDependentAmount())
                .setEmploymentDTO(scoringData.getEmployment())
                .setAccount(scoringData.getAccount())
                .setCredit(credit));

        adminService.updateApplicationStatus(application, CC_APPROVED, AUTOMATIC);

        applicationRepository.save(application.setCredit(credit));

        log.info("calculateCredit(), updated application={}", application);

////                TODO
//        measureService.incrementStatusCounter(CC_APPROVED);

        documentService.createDocumentsRequest(applicationId);
    }

    public void applyOffer(LoanOfferDTO loanOfferDTO) {
        Application application = applicationRepository.getById(loanOfferDTO.getApplicationId());

        adminService.updateApplicationStatus(application, APPROVED, MANUAL);

       application
               .setAppliedOffer(loanOfferDTO);

        applicationRepository.save(application);

//        //        TODO
//        measureService.incrementStatusCounter(application.getStatus());

        EmailMessage message = new EmailMessage()
                .address(application.getClient().getEmail())
                .applicationId(application.getId())
                .theme(EmailMessage.ThemeEnum.FINISH_REGISTRATION);
        log.info("applyOffer - start sending message to dossier message = {}", message);
        dossierService.sendMessage(message);
        log.info("applyOffer - message sent to dossier");

        log.info("applyOffer - end, updatedApplication={}", application);
    }

    private Client createClientByRequest(LoanApplicationRequestDTO request) {
        return new Client()
                .setFirstName(request.getFirstName())
                .setMiddleName(request.getMiddleName())
                .setLastName(request.getLastName())
                .setBirthDate(request.getBirthdate())
                .setPassportInfo(new PassportInfo()
                        .series(request.getPassportSeries())
                        .number(request.getPassportNumber()))
                .setEmail(request.getEmail());
    }

    private void denyApplication(Application application,
                                 Client client,
                                 ScoringDataDTO scoringData,
                                 PassportInfo passportInfo,
                                 ApplicationStatusHistoryDTO.ChangeTypeEnum changeType) {

        adminService.updateApplicationStatus(application, CC_DENIED, changeType);
        applicationRepository.save(application);


////        TODO
//        measureService.incrementStatusCounter(CC_DENIED);

        clientRepository.save(client
                .setGender(scoringData.getGender().name())
                .setPassportInfo(passportInfo)
                .setMaritalStatus(scoringData.getMaritalStatus().name())
                .setDependentAmount(scoringData.getDependentAmount())
                .setEmploymentDTO(scoringData.getEmployment())
                .setAccount(scoringData.getAccount()));

        dossierService.sendMessage(new EmailMessage()
                .theme(EmailMessage.ThemeEnum.APPLICATION_DENIED)
                .applicationId(application.getId())
                .address(application.getClient().getEmail()));
    }
}
