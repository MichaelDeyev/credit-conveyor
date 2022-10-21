package ru.deyev.credit.conveyor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.deyev.credit.conveyor.exception.ScoringException;
import ru.deyev.credit.conveyor.model.CreditDTO;
import ru.deyev.credit.conveyor.model.EmploymentDTO;
import ru.deyev.credit.conveyor.model.PaymentScheduleElement;
import ru.deyev.credit.conveyor.model.ScoringDataDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ScoringService {

    private static final String FUNDING_RATE = "15.00";

    //    using for calculating, this value updates during scoring
    private static BigDecimal CURRENT_RATE = new BigDecimal(FUNDING_RATE);

    private static final String INSURANCE_DISCOUNT = "4.00";

    private static final String SALARY_CLIENT_DISCOUNT = "1.00";

    private static final String INSURANCE_BASE_PRICE = "10000.00";

    private static final String BASE_LOAN_AMOUNT = "200000.00";

    private static final String INSURANCE_LOAN_AMOUNT_MULTIPLICAND = "0.05";

    private static final Integer BASE_PERIODS_AMOUNT_IN_YEAR = 12;

    private static final Integer DEFAULT_DECIMAL_SCALE = 2;


    public CreditDTO calculateCredit(ScoringDataDTO scoringData) {

        scoring(scoringData);

        BigDecimal totalAmount = evaluateTotalAmountByServices(scoringData.getAmount(),
                scoringData.getIsInsuranceEnabled());

        BigDecimal requestedAmount = scoringData.getAmount();

        BigDecimal rate = calculateRate(scoringData.getIsInsuranceEnabled(), scoringData.getIsSalaryClient());

        Integer term = scoringData.getTerm();

        BigDecimal monthlyPayment = calculateMonthlyPayment(totalAmount, term, rate);

        List<PaymentScheduleElement> paymentSchedule = calculatePaymentSchedule(totalAmount, scoringData.getTerm(), rate, monthlyPayment);


        return new CreditDTO()
                .amount(totalAmount)
                .monthlyPayment(calculateMonthlyPayment(totalAmount, term, rate))
                .psk(calculatePSK(paymentSchedule, requestedAmount, term))
                .paymentSchedule(paymentSchedule)
                .term(term)
                .rate(rate)
                .isInsuranceEnabled(scoringData.getIsInsuranceEnabled())
                .isSalaryClient(scoringData.getIsSalaryClient());
    }

    public BigDecimal calculateRate(Boolean isInsuranceEnabled, Boolean isSalaryClient) {
        BigDecimal rate = new BigDecimal(CURRENT_RATE.toString());

        if (isInsuranceEnabled) {
            rate = rate.subtract(new BigDecimal(INSURANCE_DISCOUNT));
        }
        if (isSalaryClient) {
            rate = rate.subtract(new BigDecimal(SALARY_CLIENT_DISCOUNT));
        }

        return rate;
    }

    public BigDecimal evaluateTotalAmountByServices(BigDecimal amount, Boolean isInsuranceEnabled) {
        if (isInsuranceEnabled) {
            BigDecimal insurancePrice = new BigDecimal(INSURANCE_BASE_PRICE);
            if (amount.compareTo(new BigDecimal(BASE_LOAN_AMOUNT)) > 0) {
                insurancePrice = insurancePrice
                        .add(amount
                                .multiply(new BigDecimal(INSURANCE_LOAN_AMOUNT_MULTIPLICAND)));
            }
            return amount.add(insurancePrice);
        } else {
            return amount;
        }
    }

/**
 * Формула расчета аннуитетного платежа:
 * ЕП = СК * КА, где
 * СК - сумма кредита
 * КА - коэффициент аннуитета
 * КА = (МП * К)/(К-1), где
 * МП - месячная процентная ставка
 * К = (1 + МП)^КП, где
 * КП - количество платежей
*/
    public BigDecimal calculateMonthlyPayment(BigDecimal totalAmount, Integer term, BigDecimal rate) {
        log.info("-------------- Calculating monthly payment --------------");
        log.info("totalAmount = {}, term = {}, rate = {}", totalAmount, term, rate);

        BigDecimal monthlyRateAbsolute = rate.divide(BigDecimal.valueOf(100), 5, RoundingMode.CEILING);
        log.info("monthlyRateAbsolute = {}", monthlyRateAbsolute);

        BigDecimal monthlyRate = monthlyRateAbsolute.divide(new BigDecimal(BASE_PERIODS_AMOUNT_IN_YEAR), 6, RoundingMode.CEILING);
        log.info("monthlyRate = {}", monthlyRate);

        BigDecimal intermediateCoefficient = (BigDecimal.ONE.add(monthlyRate)).pow(term)
                .setScale(5, RoundingMode.CEILING);
        log.info("intermediateCoefficient = {}", intermediateCoefficient);

        BigDecimal annuityCoefficient = monthlyRate.multiply(intermediateCoefficient)
                .divide(intermediateCoefficient.subtract(BigDecimal.ONE), RoundingMode.CEILING);
        log.info("annuityCoefficient = {}", annuityCoefficient);

        BigDecimal monthlyPayment = totalAmount.multiply(annuityCoefficient).setScale(2, RoundingMode.CEILING);
        log.info("monthlyPayment = {}", monthlyPayment);
        log.info("-------------- End calculating monthly payment --------------");
        return monthlyPayment;
    }

        /**
         * Формула расчета ПСК:
         * (СВ/СЗ - 1)/Г * 100, где
         * СВ — сумма всех выплат;
         * СЗ — сумма займа;
         * Г — срок кредитования в годах;
         */
    public BigDecimal calculatePSK(List<PaymentScheduleElement> paymentSchedule, BigDecimal requestedAmount, Integer term) {

        log.info("-------------- Calculating PSK --------------");
        BigDecimal paymentAmount = paymentSchedule
                .stream()
                .map(PaymentScheduleElement::getTotalPayment)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        log.info("paymentAmount = {}", paymentAmount);


        BigDecimal termInYears = divideWithScaleAndRoundingMode(BigDecimal.valueOf(term),
                new BigDecimal(BASE_PERIODS_AMOUNT_IN_YEAR));
        log.info("termInYears = {}", termInYears);

        BigDecimal intermediateCoefficient = divideWithScaleAndRoundingMode(paymentAmount, requestedAmount)
                .subtract(BigDecimal.ONE);
        log.info("intermediateCoefficient = {}", intermediateCoefficient);

        BigDecimal psk = intermediateCoefficient.divide(termInYears, 3, RoundingMode.CEILING)
                .multiply(BigDecimal.valueOf(100));
        log.info("psk = {}", psk);
        log.info("-------------- End calculating PSK --------------");

        return psk;
    }

    public List<PaymentScheduleElement> calculatePaymentSchedule(BigDecimal totalAmount, Integer term,
                                                                 BigDecimal rate, BigDecimal monthlyPayment) {
        BigDecimal remainingDebt = totalAmount.setScale(2, RoundingMode.CEILING);
        List<PaymentScheduleElement> paymentSchedule = new ArrayList<>();
        LocalDate paymentDate = LocalDate.now();

//      Add initial payment to payment schedule
        paymentSchedule.add(new PaymentScheduleElement()
                .number(0)
                .date(paymentDate)
                .totalPayment(BigDecimal.ZERO)
                .remainingDebt(remainingDebt)
                .interestPayment(BigDecimal.ZERO)
                .debtPayment(BigDecimal.ZERO));

        for (int i = 1; i < term + 1; i++) {
            paymentDate = paymentDate.plusMonths(1);

            BigDecimal interestPayment = calculateInterest(remainingDebt, rate).setScale(2, RoundingMode.CEILING);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);

            remainingDebt = remainingDebt.subtract(debtPayment);

            paymentSchedule.add(new PaymentScheduleElement()
                    .number(i)
                    .date(paymentDate)
                    .totalPayment(monthlyPayment)
                    .remainingDebt(remainingDebt)
                    .interestPayment(interestPayment)
                    .debtPayment(debtPayment));
        }

        return paymentSchedule;
    }

    public BigDecimal calculateInterest(BigDecimal remainingDebt, BigDecimal rate) {
        BigDecimal monthlyRateAbsolute = rate.divide(BigDecimal.valueOf(100), RoundingMode.CEILING);

        BigDecimal monthlyRate = monthlyRateAbsolute.divide(new BigDecimal(BASE_PERIODS_AMOUNT_IN_YEAR), 10, RoundingMode.CEILING);

        return remainingDebt.multiply(monthlyRate);
    }

    public BigDecimal divideWithScaleAndRoundingMode(BigDecimal number, BigDecimal divisor) {
        return number.divide(divisor, DEFAULT_DECIMAL_SCALE, RoundingMode.CEILING);
    }

    /** Правила скоринга:
     *  Безработный -> отказ; Самозанятый -> ставка увеличивается на 1; Владелец бизнеса -> ставка увеличивается на 3
     *  Позиция Менеджер среднего звена -> ставка уменьшается на 2; Позиция Топ-менеджер -> ставка уменьшается на 4
     *  Сумма займа > зарплата*20 -> отказ
     *  Замужем/Женат -> ставка уменьшается на 3; Разведен -> ставка увеличивается на 1
     *  Количество иждивенцев > 1 -> ставка увеличивается на 1
     *  Возраст < 20 или > 60 -> отказ
     *  Срок кредита < 1 год -> ставка увеличивается на 5; Срок кредита >= 10 лет -> ставка уменьшается на 2
     *  Пол не бинарный - ставка увеличивается на 3; Женщина от 35 до 60 лет - ставка уменьшается на 3; Мужчина от 30 до 55 лет - ставка уменьшается на 3
     *  Общий стаж работы < 1 года - отказ; Текущий стаж работы < 3 месяцев - отказ
     */
    public void scoring(ScoringDataDTO scoringData) {

        log.info("-------------- Start scoring process for client {} {} {} --------------",
                scoringData.getLastName(), scoringData.getFirstName(), scoringData.getMiddleName());

        List<String> scoringRefuseCauses = new ArrayList<>();

        EmploymentDTO employment = scoringData.getEmployment();

        BigDecimal currentRate = new BigDecimal(FUNDING_RATE);

        if (employment.getEmploymentStatus() == EmploymentDTO.EmploymentStatusEnum.UNEMPLOYED) {
            scoringRefuseCauses.add("Refuse cause: Client unemployed.");
        } else if (employment.getEmploymentStatus() == EmploymentDTO.EmploymentStatusEnum.SELF_EMPLOYED) {
            log.info("Funding rate increases by 1 percent point because employment status = SELF_EMPLOYED");
            currentRate = currentRate.add(BigDecimal.ONE);
        } else if (employment.getEmploymentStatus() == EmploymentDTO.EmploymentStatusEnum.BUSINESS_OWNER) {
            log.info("Funding rate increases by 3 percent point because employment status = BUSINESS_OWNER");
            currentRate = currentRate.add(BigDecimal.valueOf(3));
        }

        if (employment.getPosition() == EmploymentDTO.PositionEnum.MID_MANAGER) {
            log.info("Funding rate decreases by 2 percent point because employment position = MID_MANAGER");
            currentRate = currentRate.subtract(BigDecimal.valueOf(2));
        }

        if (employment.getPosition() == EmploymentDTO.PositionEnum.TOP_MANAGER) {
            log.info("Funding rate decreases by 4 percent point because employment position = TOP_MANAGER");
            currentRate = currentRate.subtract(BigDecimal.valueOf(4));
        }

        if (scoringData.getAmount()
                .compareTo(employment.getSalary().multiply(BigDecimal.valueOf(20))) > 0) {
            scoringRefuseCauses.add("Refuse cause: Too much loan amount due to client's salary.");
        }

        if (scoringData.getDependentAmount() > 1) {
            log.info("Funding rate increases by 1 percent point because dependent amount > 1");
            currentRate = currentRate.add(BigDecimal.ONE);
        }

        if (scoringData.getMaritalStatus() == ScoringDataDTO.MaritalStatusEnum.MARRIED) {
            log.info("Funding rate decreases by 3 percent point because marital status = MARRIED");
            currentRate = currentRate.subtract(BigDecimal.valueOf(3));
        } else if (scoringData.getMaritalStatus() == ScoringDataDTO.MaritalStatusEnum.DIVORCED) {
            log.info("Funding rate increases by 1 percent point because marital status = DIVORCED");
            currentRate = currentRate.add(BigDecimal.ONE);
        }

        long clientAge = ChronoUnit.YEARS.between(scoringData.getBirthdate(), LocalDate.now());
        if (clientAge > 60) {
            scoringRefuseCauses.add("Refuse cause: Client too old.");
        } else if (clientAge < 20) {
            scoringRefuseCauses.add("Refuse cause: Client too young.");
        }

        if (scoringData.getTerm() < 12) {
            log.info("Funding rate increases by 5 percent point because loan term < 12 months");
            currentRate = currentRate.add(BigDecimal.valueOf(5));
        } else if (scoringData.getTerm() >= 120) {
            log.info("Funding rate decreases by 2 percent point because loan term >= 120 months");
            currentRate = currentRate.subtract(BigDecimal.valueOf(2));
        }

        if (scoringData.getGender() == ScoringDataDTO.GenderEnum.NON_BINARY) {
            log.info("Funding rate increases by 3 percent point because gender = NON_BINARY");
            currentRate = currentRate.add(BigDecimal.valueOf(3));
        } else if (scoringData.getGender() == ScoringDataDTO.GenderEnum.FEMALE && (clientAge > 35 && clientAge < 60)) {
            log.info("Funding rate decreases by 3 percent point because gender = FEMALE and age between 35 and 60");
            currentRate = currentRate.subtract(BigDecimal.valueOf(3));
        } else if (scoringData.getGender() == ScoringDataDTO.GenderEnum.FEMALE && (clientAge > 30 && clientAge < 55)) {
            log.info("Funding rate decreases by 3 percent point because gender = MALE and age between 30 and 55");
            currentRate = currentRate.subtract(BigDecimal.valueOf(3));
        }

        if (employment.getWorkExperienceTotal() < 12) {
            scoringRefuseCauses.add("Refuse cause: Too small total working experience.");
        }
        if (employment.getWorkExperienceCurrent() < 3) {
            scoringRefuseCauses.add("Refuse cause: Too small current working experience.");
        }

        if (scoringRefuseCauses.size() > 0) {
            log.info("Scoring errors: {}", Arrays.deepToString(scoringRefuseCauses.toArray()));
        }

        log.info("-------------- End scoring process for client {} {} {} --------------",
                scoringData.getLastName(), scoringData.getFirstName(), scoringData.getMiddleName());

        if (scoringRefuseCauses.size() > 0) {
            throw new ScoringException(Arrays.deepToString(scoringRefuseCauses.toArray()));
        }

        CURRENT_RATE = currentRate;

    }
}
