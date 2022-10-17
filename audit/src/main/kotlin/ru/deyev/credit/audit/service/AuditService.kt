package ru.deyev.credit.audit.service

import org.springframework.stereotype.Service
import ru.deyev.credit.audit.entity.Audit
import ru.deyev.credit.audit.repository.AuditRepository
import java.util.*

@Service
class AuditService(
    private val auditRepository: AuditRepository
) {

    fun save(audit: Audit){
        auditRepository.save(audit)
    }

    fun getById(uuid: UUID): Audit {
        return auditRepository.findById(uuid).orElseThrow { RuntimeException("Audit with uuid $uuid doesn't exists") }
    }

    fun getAll(): Iterable<Audit> {
        return auditRepository.findAll().toList().sortedBy { it.time }
    }
}