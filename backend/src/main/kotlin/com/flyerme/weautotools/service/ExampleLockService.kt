package com.flyerme.weautotools.service

import com.flyerme.weautotools.annotation.Lock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 分布式锁使用示例Service (简化版)
 * 展示基本的@Lock注解使用方式
 *
 * @author WeAutoTools Team
 * @version 2.0.0
 * @since 2025-08-18
 */
@Service
open class ExampleLockService {

    companion object {
        private val logger = LoggerFactory.getLogger(ExampleLockService::class.java)
    }

    /**
     * 基本用户操作锁
     */
    @Lock(key = "'user:' + #userId")
    open fun userOperation(userId: Long): String {
        logger.info("执行用户操作: {}", userId)
        Thread.sleep(500) // 模拟业务处理
        return "用户操作完成: $userId"
    }

    /**
     * 用户资料更新锁
     */
    @Lock(key = "'user:profile:' + #request.userId")
    open fun updateUserProfile(request: UserProfileRequest): String {
        logger.info("更新用户资料: {}", request.userId)
        Thread.sleep(300) // 模拟更新处理
        return "用户资料更新完成: ${request.userId}"
    }

    /**
     * 业务操作锁（带自定义错误消息）
     */
    @Lock(
        key = "'business:' + #businessId",
        failMessage = "业务操作正在进行中，请稍后重试"
    )
    open fun businessOperation(businessId: String): String {
        logger.info("执行业务操作: {}", businessId)
        Thread.sleep(400)
        return "业务操作完成: $businessId"
    }

    // 数据类定义
    data class UserProfileRequest(
        val userId: Long,
        val name: String,
        val email: String
    )
}
