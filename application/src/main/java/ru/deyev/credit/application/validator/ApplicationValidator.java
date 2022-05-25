package ru.deyev.credit.application.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.deyev.credit.application.exception.PreScoringException;
import ru.deyev.credit.application.model.LoanApplicationRequestDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ApplicationValidator {

    /*Правила прескоринга:
        Имя, Фамилия - от 2 до 30 латинских букв. Отчество, при наличии - от 2 до 30 латинских букв.
        Сумма кредита - действительно число, большее или равное 10000.
        Срок кредита - целое число, большее или равное 6.
        Дата рождения - число в формате гггг-мм-дд, не позднее 18 лет с текущего дня.
        Email адрес - строка, подходящая под паттерн [\w\.]{2,50}@[\w\.]{2,20}
        Серия паспорта - 4 цифры, номер паспорта - 6 цифр.
    * */
    public void preScoring(LoanApplicationRequestDTO request) {
        log.info("-------------- Start pre-scoring process for client {} {} {} --------------",
                request.getLastName(), request.getFirstName(), request.getMiddleName());

        List<String> scoringRefuseCauses = new ArrayList<>();

        if (!request.getFirstName().matches("[A-Za-z\\-]{2,30}")) {
            scoringRefuseCauses.add("Client's first name has incorrect format.");
        }
        if (!request.getLastName().matches("[A-Za-z\\-]{2,30}")) {
            scoringRefuseCauses.add("Client's last name has incorrect format.");
        }
        if (request.getMiddleName() != null && !request.getMiddleName().matches("[A-Za-z\\-]{2,30}")) {
            scoringRefuseCauses.add("Client's middle name has incorrect format.");
        }
        if (!request.getEmail().matches("[\\w\\.]{2,50}@[\\w\\.]{2,20}")) {
            scoringRefuseCauses.add("Client's email has incorrect format.");
        }
        if (!request.getPassportSeries().matches("\\d{4}")) {
            scoringRefuseCauses.add("Client's passport series has incorrect format.");
        }
        if (!request.getPassportNumber().matches("\\d{6}")) {
            scoringRefuseCauses.add("passport number has incorrect format.");
        }
        if (request.getAmount().compareTo(BigDecimal.valueOf(10000)) < 0) {
            scoringRefuseCauses.add("Credit amount less than 10000.");
        }
        if (request.getTerm() < 6) {
            scoringRefuseCauses.add("Credit term less than 6.");
        }
        if (ChronoUnit.YEARS.between(request.getBirthdate(), LocalDate.now()) < 18) {
            scoringRefuseCauses.add("Client's age less than 18.");
        }

        if (scoringRefuseCauses.size() > 0) {
            throw new PreScoringException("Pre-scoring failed by these causes: "
                    + Arrays.deepToString(scoringRefuseCauses.toArray()));
        }

    }
}
