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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Accessors(chain = true)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@SequenceGenerator(name = "applicationSeqGenerator", sequenceName = "application_id_seq", allocationSize = 1)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "applicationSeqGenerator")
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "credit_id", referencedColumnName = "id")
    private Credit credit;

    @Column
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column
    private LocalDate creationDate;

    @Column
    private LocalDate signDate;

    @Column
    private Integer sesCode;

    @Column
    @Type(type = "jsonb")
    private List<ApplicationStatusHistoryDTO> statusHistory;

    @Column
    @Type(type = "jsonb")
    private LoanOfferDTO appliedOffer;

    public void updateApplicationStatus(ApplicationStatus newStatus,
                                        ApplicationStatusHistoryDTO.ChangeTypeEnum changeType) {

        List<ApplicationStatusHistoryDTO> updatedStatusHistory = updateStatusHistory(
                this.getStatusHistory(),
                newStatus,
                changeType
        );

        this.setStatus(newStatus)
                .setStatusHistory(updatedStatusHistory);
    }

    private List<ApplicationStatusHistoryDTO> updateStatusHistory(List<ApplicationStatusHistoryDTO> history,
                                                                  ApplicationStatus newStatus,
                                                                  ApplicationStatusHistoryDTO.ChangeTypeEnum changeType) {
        if (history == null) {
            history = new ArrayList<>();
        }

        history.add(new ApplicationStatusHistoryDTO()
                .status(newStatus)
                .time(LocalDateTime.now())
                .changeType(changeType));
        return history;
    }
}
