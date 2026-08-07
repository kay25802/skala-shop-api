package com.sk.skala.shopapi.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApiLogAspect {

    @Around("execution(* com.sk.skala.shopapi.controller..*(..))")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            log.info("API {} completed in {} ms", joinPoint.getSignature().toShortString(), elapsed);
        }
    }
}
