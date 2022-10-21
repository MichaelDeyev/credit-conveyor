package ru.deyev.credit.deal.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final static String KAFKA_TOPIC_NAME = "audit-action";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(AuditAction)")
    public Object sendAuditAction(ProceedingJoinPoint call) throws Throwable {

        processAudit(call, AuditActionType.START);

        Object callResult = null;

        try {

            callResult = call.proceed();

        } catch (Exception e) {

            processAudit(call, AuditActionType.FAILURE);
            throw e;
        }

        processAudit(call, AuditActionType.SUCCESS);
        return callResult;

    }

    private void processAudit(ProceedingJoinPoint call, AuditActionType actionType) {
        Object[] callArgs = call.getArgs();
        Signature signature = call.getSignature();
        String auditMessage = createAuditMessage(callArgs, signature);

        AuditModel auditModel = createAuditModel(actionType, auditMessage);

        log.info("Audit [" + actionType + "] handling with auditModel = {}", auditModel);

        kafkaTemplate.send(KAFKA_TOPIC_NAME, toJson(auditModel));
    }

    private String createAuditMessage(Object[] args, Signature signature) {
        return "Audit Action for method \"" + signature.getName()
                + "\" with params " + Arrays.deepToString(args);
    }

    private String toJson(AuditModel model) {
        try {
            return objectMapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private AuditModel createAuditModel(AuditActionType actionType, String message) {
        return AuditModel.builder()
                .uuid(UUID.randomUUID())
                .auditServiceType(AuditServiceType.DEAL)
                .actionType(actionType)
                .time(LocalDateTime.now())
                .message(message)
                .build();
    }
}
