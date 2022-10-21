package ru.deyev.credit.dossier.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.deyev.credit.dossier.feign.DealFeignClient;
import ru.deyev.credit.dossier.model.ApplicationDTO;
import ru.deyev.credit.dossier.model.ClientDTO;
import ru.deyev.credit.dossier.model.CreditDTO;
import ru.deyev.credit.dossier.model.DocumentType;
import ru.deyev.credit.dossier.model.EmploymentDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrintedFormService {

    private final static String FILE_EXTENSION = ".txt";

    @Value("${custom.document.credit-contract}")
    private String CREDIT_CONTRACT_TEXT;
    @Value("${custom.document.credit-payment-schedule}")
    private String CREDIT_PAYMENT_SCHEDULE_TEXT;
    @Value("${custom.document.credit-application}")
    private String CREDIT_APPLICATION_TEXT;
    private final DealFeignClient dealFeignClient;

    private Map<String, String> bookmarks;

    public List<File> createDocuments(Long applicationId) {
        ApplicationDTO application = dealFeignClient.getApplicationById(applicationId);
        fillBookmarks(application);

        File creditContract = createDocument(DocumentType.CREDIT_CONTRACT);
        File creditPaymentSchedule = createDocument(DocumentType.CREDIT_PAYMENT_SCHEDULE);
        File creditApplication = createDocument(DocumentType.CREDIT_APPLICATION);

        return List.of(creditContract, creditApplication, creditPaymentSchedule);
    }

    public File createDocument(DocumentType documentType) {
        String currentDocumentText = "";

        switch (documentType) {
            case CREDIT_CONTRACT:
                currentDocumentText = CREDIT_CONTRACT_TEXT;
                break;
            case CREDIT_APPLICATION:
                currentDocumentText = CREDIT_APPLICATION_TEXT;
                break;
            case CREDIT_PAYMENT_SCHEDULE:
                currentDocumentText = CREDIT_PAYMENT_SCHEDULE_TEXT;
                break;
        }

        log.info("For documentType = {}, text before population with bookmarks = {}", documentType, currentDocumentText);

        currentDocumentText = populateTextWithData(currentDocumentText);

        log.info("For documentType = {}, text after population with bookmarks = {}", documentType, currentDocumentText);

        String fileName = documentType.name().toLowerCase().replaceAll("_", "-");

        File currentFile;
        try {
            Path currentFilePath = Files.createTempFile(fileName, FILE_EXTENSION);

            currentFile = currentFilePath.toFile();

            FileWriter writer = new FileWriter(currentFile);

            writer.write(currentDocumentText);

            writer.flush();
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return currentFile;
    }

    private String populateTextWithData(String text) {

        String tmpText = text;
        String populatedText;

//      Not so effective, but works
        for (Map.Entry<String, String> pair : bookmarks.entrySet()) {
            String bookmark = pair.getKey();
            String bookmarkValue = pair.getValue();

            tmpText = tmpText.replaceAll("\\{" + bookmark + "\\}", bookmarkValue);
        }

        populatedText = tmpText;

        return populatedText;
    }

    private void fillBookmarks(ApplicationDTO application) {
        bookmarks = new HashMap<>();
        CreditDTO credit = application.getCredit();
        ClientDTO client = application.getClient();
        EmploymentDTO employment = client.getEmployment();

        bookmarks.put("creditId", credit.getId().toString());
        bookmarks.put("creditDate", application.getCreationDate().toString());
        bookmarks.put("applicationCreationDate", application.getCreationDate().toString());
        bookmarks.put("clientFullName", client.getLastName() +
                " " + client.getFirstName() +
                " " + client.getMiddleName());
        bookmarks.put("clientPassport", client.getPassportSeries() +
                " " + client.getPassportNumber() +
                " issued " + client.getPassportIssueDate().toString() +
                " branch code " + client.getPassportIssueBranch());
        bookmarks.put("creditAmount", credit.getAmount().toString());
        bookmarks.put("creditTerm", credit.getTerm().toString());
        bookmarks.put("monthlyPayment", credit.getMonthlyPayment().toString());
        bookmarks.put("rate", credit.getRate().toString());
        bookmarks.put("psk", credit.getPsk().toString());
        bookmarks.put("isInsuranceEnabled", credit.getIsInsuranceEnabled().toString());
        bookmarks.put("isSalaryClient", credit.getIsSalaryClient().toString());
        bookmarks.put("paymentSchedule", Arrays.deepToString(credit.getPaymentSchedule().toArray()));
        bookmarks.put("applicationId", application.getId().toString());
        bookmarks.put("clientBirthdate", client.getBirthdate().toString());
        bookmarks.put("clientGender", client.getGender());
        bookmarks.put("clientEmail", client.getEmail());
        bookmarks.put("clientMartialStatus", client.getMaritalStatus());
        bookmarks.put("clientDependentAmount", client.getDependentAmount().toString());
        bookmarks.put("employmentStatus", employment.getEmploymentStatus().name());
        bookmarks.put("employerINN", employment.getEmployerINN());
        bookmarks.put("employmentSalary", employment.getSalary().toString());
        bookmarks.put("employmentPosition", employment.getPosition().name());
        bookmarks.put("employmentWorkExperienceTotal", employment.getWorkExperienceTotal().toString());
        bookmarks.put("employmentWorkExperienceCurrent", employment.getWorkExperienceCurrent().toString());

    }
}
