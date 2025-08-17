package com.flyerme.weautotools.aspect;

import com.flyerme.weautotools.annotation.DistributedLock;
import com.flyerme.weautotools.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 分布式锁AOP切面
 * 处理@DistributedLock注解的方法
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Slf4j
@Aspect
@Component
public class DistributedLockAspect {

    @Autowired
    private RedissonClient redissonClient;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = generateLockKey(joinPoint, distributedLock);
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            // 尝试获取锁
            acquired = lock.tryLock(
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!acquired) {
                log.warn("获取分布式锁失败: {}", lockKey);
                throw new BusinessException(distributedLock.failMessage());
            }

            log.debug("成功获取分布式锁: {}", lockKey);
            
            // 执行目标方法
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", lockKey, e);
            throw new BusinessException("获取分布式锁被中断");
        } finally {
            // 自动释放锁
            if (acquired && distributedLock.autoRelease() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁: {}", lockKey);
            }
        }
    }

    /**
     * 生成锁的key
     */
    private String generateLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        String keyExpression = distributedLock.key();
        
        // 如果key不包含SpEL表达式，直接返回
        if (!keyExpression.contains("#") && !keyExpression.contains("'")) {
            return distributedLock.prefix() + ":" + keyExpression;
        }

        // 解析SpEL表达式
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Object[] args = joinPoint.getArgs();
            String[] paramNames = nameDiscoverer.getParameterNames(method);

            // 创建SpEL上下文
            EvaluationContext context = new StandardEvaluationContext();
            
            // 设置方法参数
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            // 解析表达式
            Expression expression = parser.parseExpression(keyExpression);
            Object keyValue = expression.getValue(context);
            
            return distributedLock.prefix() + ":" + keyValue;
            
        } catch (Exception e) {
            log.error("解析锁key表达式失败: {}", keyExpression, e);
            // 如果解析失败，使用方法名作为key
            String methodName = joinPoint.getSignature().getName();
            return distributedLock.prefix() + ":" + methodName;
        }
    }
}
