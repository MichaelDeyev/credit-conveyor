package ru.deyev.credit.deal.audit;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
public class AuditModel {
    private final UUID uuid;
    private final AuditActionType actionType;
    private final AuditServiceType auditServiceType;
    private final LocalDateTime time;
    private final String message;
}
