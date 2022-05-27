package ru.deyev.credit.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.deyev.credit.deal.model.Credit;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {

}
