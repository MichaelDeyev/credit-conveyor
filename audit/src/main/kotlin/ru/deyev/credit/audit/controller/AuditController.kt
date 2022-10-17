package ru.deyev.credit.audit.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.deyev.credit.audit.entity.Audit
import ru.deyev.credit.audit.service.AuditService
import java.util.*

@RestController
@RequestMapping("/audit")
class AuditController(
    private val auditService: AuditService
) {

    @GetMapping("/{id}")
    fun getAuditActionById(@PathVariable id: UUID): ResponseEntity<Audit> {
        return ResponseEntity.ok(auditService.getById(id))
    }

    @GetMapping("/all", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllAuditActions(): ResponseEntity<Iterable<Audit>> {
        return ResponseEntity.ok(auditService.getAll())
    }
}