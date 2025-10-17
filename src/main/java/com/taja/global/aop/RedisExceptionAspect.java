package com.taja.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class RedisExceptionAspect {

    @AfterThrowing(
            pointcut = "execution(* com.taja.station.infra.StationRedisRepositoryImpl.*(..))",
            throwing = "ex"
    )
    public void handleDataAccessException(JoinPoint joinPoint, DataAccessException ex) {
        String methodName = joinPoint.getSignature().toShortString();

        log.error("Redis 연결 또는 데이터 접근 실패 [{}]: {}", methodName, ex.getMessage());
        throw new RuntimeException("Redis 처리 중 오류가 발생했습니다.", ex);
    }

}
