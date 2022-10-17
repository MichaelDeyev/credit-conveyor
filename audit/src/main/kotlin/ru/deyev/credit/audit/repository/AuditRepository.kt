package ru.deyev.credit.audit.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.deyev.credit.audit.entity.Audit
import java.util.UUID

@Repository
interface AuditRepository : CrudRepository<Audit, UUID> {
}