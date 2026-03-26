package com.taja.application.station.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisherHelper {

    private final ApplicationEventPublisher eventPublisher;

    public void publishEventAfterCommit(Object event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            eventPublisher.publishEvent(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    eventPublisher.publishEvent(event);
                    log.debug("트랜잭션 커밋 후 이벤트 발행 완료: {}", event.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("트랜잭션 커밋 후 이벤트 발행 실패: {}", e.getMessage(), e);
                }
            }
        });
    }
}
