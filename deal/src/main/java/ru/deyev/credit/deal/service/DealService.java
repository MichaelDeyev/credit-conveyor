package ru.deyev.credit.deal.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.deyev.credit.deal.feign.ConveyorFeignClient;
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

@Service
@AllArgsConstructor
@Slf4j
public class DealService {

    private final ConveyorFeignClient conveyorFeignClient;

    private final ApplicationRepository applicationRepository;

    private final ClientRepository clientRepository;

    private final DocumentService documentService;

    private final DossierService dossierService;

    private final CreditRepository creditRepository;

    public List<LoanOfferDTO> createApplication(@RequestBody LoanApplicationRequestDTO request) {
        Client newClient = createClientByRequest(request);

        Client savedClient = clientRepository.save(newClient);

        log.info("createApplication(), savedClient={}", savedClient);
        Application newApplication = new Application()
                .setClient(savedClient)
                .setCreationDate(LocalDate.now())
                .setStatus(PREAPPROVAL)
                .setStatusHistory(List.of(
                        new ApplicationStatusHistoryDTO()
                                .status(PREAPPROVAL)
                                .time(LocalDateTime.now())
                                .changeType(AUTOMATIC)));

        Application savedApplication = applicationRepository.save(newApplication);
        clientRepository.save(savedClient.setApplication(savedApplication));

        List<LoanOfferDTO> loanOffers = conveyorFeignClient.generateOffers(request).getBody();

        assert loanOffers != null;
        loanOffers.forEach(loanOfferDTO -> loanOfferDTO.setApplicationId(savedApplication.getId()));

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
        CreditDTO creditDTO = null;

        PassportInfo fullPassportInfo = new PassportInfo()
                .series(client.getPassportInfo().getSeries())
                .number(client.getPassportInfo().getNumber())
                .issueDate(scoringData.getPassportIssueDate())
                .issueBranch(scoringData.getPassportIssueBranch());


        try {
            creditDTO = conveyorFeignClient.calculateCredit(scoringData).getBody();
        } catch (Exception e) {
            log.warn("Credit conveyor denied application by these reasons: {}", e.getMessage());
            applicationRepository.save(application.setStatus(CC_DENIED));

            clientRepository.save(client
                    .setGender(scoringData.getGender().name())
                    .setPassportInfo(fullPassportInfo)
                    .setMaritalStatus(scoringData.getMaritalStatus().name())
                    .setDependentAmount(scoringData.getDependentAmount())
                    .setEmploymentDTO(scoringData.getEmployment())
                    .setAccount(scoringData.getAccount()));

            dossierService.sendMessage(new EmailMessage()
                    .theme(EmailMessage.ThemeEnum.APPLICATION_DENIED)
                    .applicationId(applicationId)
                    .address(application.getClient().getEmail()));

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

        List<ApplicationStatusHistoryDTO> updatedStatusHistory = updateStatusHistory(application.getStatusHistory(), CC_APPROVED, AUTOMATIC);

        applicationRepository.save(application
                .setStatus(CC_APPROVED)
                .setStatusHistory(updatedStatusHistory)
                .setCredit(credit));
        log.info("calculateCredit(), updated application={}", application);

        documentService.createDocumentsRequest(applicationId);
    }

    public void applyOffer(LoanOfferDTO loanOfferDTO) {
        Application application = applicationRepository.getById(loanOfferDTO.getApplicationId());
        List<ApplicationStatusHistoryDTO> updatedStatusHistory = updateStatusHistory(application.getStatusHistory(), APPROVED, MANUAL);

        Application updatedApplication = applicationRepository.save(
                application
                        .setStatus(APPROVED)
                        .setAppliedOffer(loanOfferDTO)
                        .setStatusHistory(updatedStatusHistory));

        EmailMessage message = new EmailMessage()
                .address(updatedApplication.getClient().getEmail())
                .applicationId(updatedApplication.getId())
                .theme(EmailMessage.ThemeEnum.FINISH_REGISTRATION);
        log.info("applyOffer - start sending message to dossier message = {}", message);
        dossierService.sendMessage(message);
        log.info("applyOffer - message sent to dossier");

        log.info("applyOffer - end, updatedApplication={}", updatedApplication);
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

    private List<ApplicationStatusHistoryDTO> updateStatusHistory(List<ApplicationStatusHistoryDTO> previous,
                                                                  ApplicationStatus newStatus,
                                                                  ApplicationStatusHistoryDTO.ChangeTypeEnum changeType) {
        previous.add(new ApplicationStatusHistoryDTO()
                .status(newStatus)
                .time(LocalDateTime.now())
                .changeType(changeType));
        return previous;
    }
}
