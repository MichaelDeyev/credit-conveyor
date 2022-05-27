package ru.deyev.credit.deal.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Accessors(chain = true)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@SequenceGenerator(name = "creditSeqGenerator", sequenceName = "credit_id_seq", allocationSize = 1)
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "creditSeqGenerator")
    private Long id;

    @Column
    private BigDecimal amount;

    @Column
    private Integer term;

    @Column
    private BigDecimal monthlyPayment;

    @Column
    private BigDecimal rate;

    @Column
    private BigDecimal psk;

    @Column
    private Boolean isInsuranceEnabled;

    @Column
    private Boolean isSalaryClient;

    @Column
    @Type(type = "jsonb")
    private List<PaymentScheduleElement> paymentSchedule;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "application_id", referencedColumnName = "id")
    private Application application;

    @Column
    @Enumerated(EnumType.STRING)
    private CreditStatus creditStatus;
}
