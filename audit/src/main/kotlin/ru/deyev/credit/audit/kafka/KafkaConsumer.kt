package ru.deyev.credit.audit.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.deyev.credit.audit.entity.Audit
import ru.deyev.credit.audit.service.AuditService

private val logger =  KotlinLogging.logger{}

@Service
class KafkaConsumer(
    private val auditService: AuditService,
    private val objectMapper: ObjectMapper
) {

    @KafkaListener(topics = ["audit-action"], groupId = "\${spring.kafka.consumer.group-id}")
    fun consumeMessage(message: String) {
        logger.info("Consume message from kafka: $message")
        auditService.save(objectMapper.readValue(message, Audit::class.java))
    }
}