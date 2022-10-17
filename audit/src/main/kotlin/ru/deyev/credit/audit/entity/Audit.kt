package ru.deyev.credit.audit.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.LocalDateTime
import java.util.*

@RedisHash("Audit")
data class Audit(
    @Id
    val uuid: UUID,
    val actionType: String,
    val auditService: String,
    val time: LocalDateTime,
    val message: String
)
