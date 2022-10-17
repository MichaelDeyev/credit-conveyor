package ru.deyev.credit.audit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuditApplication

fun main(args: Array<String>) {
    runApplication<AuditApplication>(*args)
}
