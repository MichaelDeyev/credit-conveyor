package ru.deyev.credit.deal.metric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.deyev.credit.deal.model.ApplicationStatus;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MonitoringAspect {
    private final MeasureService measureService;

    @Around("@annotation(Monitored)")
    public Object sendMonitoringAction(ProceedingJoinPoint call) throws Throwable {
        Object[] callArgs = call.getArgs();

        log.info("Method annotated with @Monitored has arguments: {}", Arrays.stream(callArgs).map(Object::getClass));

        for (Object callArg : callArgs) {
            if (callArg.getClass().equals(ApplicationStatus.class)) {
                ApplicationStatus status = (ApplicationStatus) callArg;
                log.info("Found arg of ApplicationStatus type: {}", status);
                log.info("Send measure event");
                measureService.incrementStatusCounter(status);
            }
        }

        return call.proceed();
    }
}
