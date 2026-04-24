package com.codeit.team4.deokhugam.global.lock;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final RedissonClient redissonClient;
    private final LockKeyResolver lockKeyResolver;

    @Around("@annotation(com.codeit.team4.deokhugam.global.lock.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        String lockKey = lockKeyResolver.resolve(
                distributedLock.key(),
                distributedLock.lockParam(),
                paramNames,
                joinPoint.getArgs()
        );

        RLock lock = redissonClient.getLock(lockKey);

        log.debug("락 획득 시도: {}", lockKey);

        boolean acquired = lock.tryLock(
                distributedLock.waitTime(),
                distributedLock.leaseTime(),
                distributedLock.timeUnit()
        );

        if (!acquired) {
            log.warn("락 획득 실패: {}", lockKey);
            throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED, "락 획득 실패: " + lockKey);
        }

        log.debug("락 획득 성공: {}", lockKey);

        try {
            return joinPoint.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("락 해제: {}", lockKey);
            }
        }
    }
}
