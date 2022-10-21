package ru.deyev.credit.deal.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.deyev.credit.deal.audit.AuditAction;
import ru.deyev.credit.deal.exception.DealException;
import ru.deyev.credit.deal.metric.MeasureService;
import ru.deyev.credit.deal.model.Application;
import ru.deyev.credit.deal.model.ApplicationStatus;
import ru.deyev.credit.deal.model.ApplicationStatusHistoryDTO;
import ru.deyev.credit.deal.model.Credit;
import ru.deyev.credit.deal.model.CreditStatus;
import ru.deyev.credit.deal.model.EmailMessage;
import ru.deyev.credit.deal.repository.ApplicationRepository;
import ru.deyev.credit.deal.repository.CreditRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static ru.deyev.credit.deal.model.ApplicationStatus.PREPARE_DOCUMENTS;
import static ru.deyev.credit.deal.model.ApplicationStatusHistoryDTO.ChangeTypeEnum.MANUAL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DossierService dossierService;
    private final ApplicationRepository applicationRepository;
    private final CreditRepository creditRepository;

    private final MeasureService measureService;

    public void createDocumentsRequest(Long applicationId) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application with id " + applicationId + " not found."));

        if (application.getStatus() != ApplicationStatus.CC_APPROVED) {
            throw new DealException("Application " + applicationId + " in status " + application.getStatus()
                    + ", but should be in status " + ApplicationStatus.CC_APPROVED);
        }

        log.info("Sending create document request for application {}, to email {}",
                application, application.getClient().getEmail());

        dossierService.sendMessage(new EmailMessage()
                .theme(EmailMessage.ThemeEnum.CREATE_DOCUMENT)
                .applicationId(applicationId)
                .address(application.getClient().getEmail()));
    }

    @AuditAction
    public void sendDocuments(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application with id " + applicationId + " not found."));

        if (application.getStatus() != ApplicationStatus.CC_APPROVED) {
            throw new DealException("Application " + applicationId + " in status " + application.getStatus()
                    + ", but should be in status " + ApplicationStatus.CC_APPROVED);
        }

        List<ApplicationStatusHistoryDTO> statusHistory = application.getStatusHistory();
        statusHistory.add(
                new ApplicationStatusHistoryDTO()
                        .status(PREPARE_DOCUMENTS)
                        .time(LocalDateTime.now())
                        .changeType(MANUAL));

        applicationRepository.save(application
                .setStatus(PREPARE_DOCUMENTS)
                .setStatusHistory(statusHistory));

        measureService.incrementStatusCounter(PREPARE_DOCUMENTS);

        log.info("Sending send document request for application {}, to email {}",
                application, application.getClient().getEmail());

        dossierService.sendMessage(new EmailMessage()
                .theme(EmailMessage.ThemeEnum.SEND_DOCUMENT)
                .applicationId(applicationId)
                .address(application.getClient().getEmail()));
    }

    @AuditAction
    public void signDocuments(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application with id " + applicationId + " not found."));

        if (application.getStatus() != ApplicationStatus.DOCUMENT_CREATED) {
            throw new DealException("Application " + applicationId + " in status " + application.getStatus()
                    + ", but should be in status " + ApplicationStatus.DOCUMENT_CREATED);
        }

        Integer sesCode = generateSesCode();

        applicationRepository.save(application.setSesCode(sesCode));

        log.info("Sending send sign document request for application {}, to email {}",
                application, application.getClient().getEmail());

        dossierService.sendMessage(new EmailMessage()
                .theme(EmailMessage.ThemeEnum.SEND_SES)
                .applicationId(applicationId)
                .address(application.getClient().getEmail()));


    }

    @AuditAction
    public void verifyCode(Long applicationId, Integer sesCode) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application with id " + applicationId + " not found."));

        if (application.getStatus() != ApplicationStatus.DOCUMENT_CREATED) {
            throw new DealException("Application " + applicationId + " in status " + application.getStatus()
                    + ", but should be in status " + ApplicationStatus.DOCUMENT_CREATED);
        }

        if (!sesCode.equals(application.getSesCode())) {
            throw new DealException("For application " + applicationId + " wrong SES code " + sesCode
                    + ". It should be in status " + application.getSesCode());
        }

        List<ApplicationStatusHistoryDTO> statusHistory = application.getStatusHistory();
        statusHistory.add(new ApplicationStatusHistoryDTO()
                .status(ApplicationStatus.DOCUMENT_SIGNED)
                .time(LocalDateTime.now())
                .changeType(ApplicationStatusHistoryDTO.ChangeTypeEnum.MANUAL));


        applicationRepository.save(application.setStatus(ApplicationStatus.DOCUMENT_SIGNED)
                .setStatusHistory(statusHistory)
                .setSignDate(LocalDate.now()));

        measureService.incrementStatusCounter(ApplicationStatus.DOCUMENT_SIGNED);

        issueCredit(applicationId);
    }

    @AuditAction
    private void issueCredit(Long applicationId) {
//        imitate long credit issuing action
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application with id " + applicationId + " not found."));

        Long creditId = application.getCredit().getId();
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new EntityNotFoundException("Credit with id " + creditId + " not found."));

        List<ApplicationStatusHistoryDTO> statusHistory = application.getStatusHistory();
        statusHistory.add(new ApplicationStatusHistoryDTO()
                .status(ApplicationStatus.CREDIT_ISSUED)
                .time(LocalDateTime.now())
                .changeType(ApplicationStatusHistoryDTO.ChangeTypeEnum.AUTOMATIC));


        applicationRepository.save(application
                .setStatus(ApplicationStatus.CREDIT_ISSUED)
                .setStatusHistory(statusHistory));

        measureService.incrementStatusCounter(ApplicationStatus.CREDIT_ISSUED);

        creditRepository.save(credit.setCreditStatus(CreditStatus.ISSUED));

        dossierService.sendMessage(new EmailMessage()
                .theme(EmailMessage.ThemeEnum.CREDIT_ISSUED)
                .applicationId(applicationId)
                .address(application.getClient().getEmail()));

        log.info("\n----------------------------------------" +
                "\n-------------CONGRATULATION!------------" +
                "\n--------CREDIT FOR APPLICATION {}-------" +
                "\n----------------ISSUED------------------" +
                "\n----------------------------------------", applicationId);

    }

    private Integer generateSesCode() {
        int max = 9999;
        int min = 1000;
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
