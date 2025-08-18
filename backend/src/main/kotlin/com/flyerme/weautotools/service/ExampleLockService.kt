package com.flyerme.weautotools.service

import com.flyerme.weautotools.annotation.Lock
import com.flyerme.weautotools.util.LockKeyGenerator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * 分布式锁使用示例Service (Kotlin版本)
 * 展示各种@Lock注解的使用方式
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Service
open class ExampleLockService {

    companion object {
        private val logger = LoggerFactory.getLogger(ExampleLockService::class.java)
    }

    /**
     * 示例1: 简单的用户操作锁
     */
    @Lock(key = "'user:operation:' + #userId")
    open fun userOperation(userId: Long): String {
        logger.info("执行用户操作: {}", userId)
        Thread.sleep(1000) // 模拟业务处理
        return "用户操作完成: $userId"
    }

    /**
     * 示例2: 订单支付锁，使用自定义超时时间
     */
    @Lock(
        key = "'order:payment:' + #orderId",
        waitTime = 5L,
        leaseTime = 60L,
        timeUnit = TimeUnit.SECONDS,
        failMessage = "订单正在支付中，请稍后重试"
    )
    open fun processPayment(orderId: String, amount: Double): String {
        logger.info("处理订单支付: {} - {}", orderId, amount)
        Thread.sleep(2000) // 模拟支付处理
        return "支付成功: $orderId"
    }

    /**
     * 示例3: 批量操作锁，使用数组参数
     */
    @Lock(key = "'batch:process:' + #ids")
    open fun batchProcess(ids: Array<String>): String {
        logger.info("批量处理: {}", ids.contentToString())
        Thread.sleep(1500) // 模拟批量处理
        return "批量处理完成: ${ids.size} 条记录"
    }

    /**
     * 示例4: 使用对象属性作为锁key
     */
    @Lock(key = "'user:profile:' + #request.userId")
    open fun updateUserProfile(request: UserProfileRequest): String {
        logger.info("更新用户资料: {}", request.userId)
        Thread.sleep(800) // 模拟更新处理
        return "用户资料更新完成: ${request.userId}"
    }

    /**
     * 示例5: 使用执行路径作为锁key
     */
    @Lock(key = "#executionPath + ':' + #resourceId")
    open fun exclusiveResourceOperation(resourceId: String): String {
        logger.info("独占资源操作: {}", resourceId)
        Thread.sleep(1200) // 模拟资源操作
        return "资源操作完成: $resourceId"
    }

    /**
     * 示例6: 手动控制锁释放
     */
    @Lock(
        key = "'manual:lock:' + #taskId",
        autoRelease = false,
        failMessage = "任务正在执行中"
    )
    open fun manualLockTask(taskId: String): String {
        logger.info("手动锁任务: {}", taskId)
        // 注意：autoRelease = false 时需要手动释放锁
        // 这里只是示例，实际使用时需要通过其他方式释放锁
        Thread.sleep(500)
        return "手动锁任务完成: $taskId"
    }

    /**
     * 示例7: 使用静态工具方法生成锁key
     */
    fun businessOperationWithUtilKey(businessId: String): String {
        val lockKey = LockKeyGenerator.generateBusinessKey("operation", businessId)
        logger.info("使用工具方法生成的锁key: {}", lockKey)
        
        // 这里可以手动使用DistributedLockUtil或者使用注解
        return "业务操作完成: $businessId"
    }

    /**
     * 示例8: 复杂的SpEL表达式
     */
    @Lock(key = "'complex:' + #request.type + ':' + #request.id + ':' + #userId")
    open fun complexOperation(request: ComplexRequest, userId: Long): String {
        logger.info("复杂操作: {} - {} - {}", request.type, request.id, userId)
        Thread.sleep(1000)
        return "复杂操作完成"
    }

    /**
     * 示例9: 条件锁 - 只有满足条件才加锁
     */
    @Lock(
        key = "'conditional:' + #data.id",
        failMessage = "数据正在处理中，请稍后重试"
    )
    open fun conditionalLockOperation(data: DataRequest): String {
        // 在实际业务中，可以在方法内部添加条件判断
        if (data.needsLock) {
            logger.info("需要锁的操作: {}", data.id)
            Thread.sleep(1000)
            return "锁定操作完成: ${data.id}"
        } else {
            logger.info("不需要锁的操作: {}", data.id)
            return "普通操作完成: ${data.id}"
        }
    }

    /**
     * 示例10: 使用列表参数
     */
    @Lock(key = "'list:operation:' + #items")
    open fun listOperation(items: List<String>): String {
        logger.info("列表操作: {}", items)
        Thread.sleep(800)
        return "列表操作完成: ${items.size} 项"
    }

    // 数据类定义
    data class UserProfileRequest(
        val userId: Long,
        val name: String,
        val email: String
    )

    data class ComplexRequest(
        val type: String,
        val id: String,
        val data: Map<String, Any>
    )

    data class DataRequest(
        val id: String,
        val needsLock: Boolean,
        val data: String
    )
}
