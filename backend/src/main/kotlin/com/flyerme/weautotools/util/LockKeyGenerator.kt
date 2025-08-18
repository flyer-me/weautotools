package com.flyerme.weautotools.util

import com.flyerme.weautotools.annotation.Lock
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.expression.EvaluationContext
import org.springframework.expression.Expression
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.security.MessageDigest

/**
 * 锁key生成器 (Kotlin版本)
 * 支持SpEL表达式和多种key生成策略
 *
 * @author WeAutoTools Team
 * @version 1.0.0
 * @since 2025-08-17
 */
@Component
open class LockKeyGenerator {

    private val parser: ExpressionParser = SpelExpressionParser()
    private val nameDiscoverer = DefaultParameterNameDiscoverer()

    companion object {
        private val logger = LoggerFactory.getLogger(LockKeyGenerator::class.java)
        private const val EXECUTION_PATH_VARIABLE = "executionPath"
        private const val MAX_KEY_LENGTH = 250 // Redis key最大长度限制

        /**
         * 静态工具方法：生成简单的锁key
         */
        @JvmStatic
        fun generateSimpleKey(prefix: String, vararg parts: String): String {
            return "$prefix:${parts.joinToString(":")}"
        }

        @JvmStatic
        fun generateUserKey(userId: Long): String {
            return generateSimpleKey("lock", "user", userId.toString())
        }

        @JvmStatic
        fun generateOrderKey(orderId: String): String {
            return generateSimpleKey("lock", "order", orderId)
        }

        @JvmStatic
        fun generateBusinessKey(business: String, identifier: String): String {
            return generateSimpleKey("lock", business, identifier)
        }
    }

    /**
     * 生成锁的key
     */
    fun generateKey(joinPoint: ProceedingJoinPoint, lock: Lock): String {
        val keyExpression = lock.key
        val prefix = lock.prefix
        
        return try {
            val evaluatedKey = when {
                // 如果key不包含SpEL表达式，直接使用
                !containsSpelExpression(keyExpression) -> keyExpression
                // 解析SpEL表达式
                else -> evaluateSpelExpression(joinPoint, keyExpression)
            }
            
            val fullKey = "$prefix:$evaluatedKey"
            
            // 如果key太长，使用hash缩短
            if (fullKey.length > MAX_KEY_LENGTH) {
                val hash = generateHash(fullKey)
                "$prefix:hash:$hash"
            } else {
                fullKey
            }
            
        } catch (e: Exception) {
            logger.error("解析锁key表达式失败: {}", keyExpression, e)
            // 如果解析失败，使用方法签名作为fallback
            generateFallbackKey(joinPoint, prefix)
        }
    }

    /**
     * 检查是否包含SpEL表达式
     */
    private fun containsSpelExpression(expression: String): Boolean {
        return expression.contains("#") || expression.contains("'") || expression.contains("T(")
    }

    /**
     * 评估SpEL表达式
     */
    private fun evaluateSpelExpression(joinPoint: ProceedingJoinPoint, keyExpression: String): String {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val args = joinPoint.args
        val paramNames = nameDiscoverer.getParameterNames(method)

        // 创建SpEL上下文
        val context: EvaluationContext = StandardEvaluationContext().apply {
            // 设置executionPath变量
            setVariable(EXECUTION_PATH_VARIABLE, generateExecutionPath(method))
            
            // 设置方法参数
            paramNames?.forEachIndexed { index, paramName ->
                if (index < args.size) {
                    setVariable(paramName, args[index])
                    // 同时支持p0, p1...格式
                    setVariable("p$index", args[index])
                    // 同时支持a0, a1...格式
                    setVariable("a$index", args[index])
                }
            }
        }

        // 解析并评估表达式
        val expression: Expression = parser.parseExpression(keyExpression)
        val result = expression.getValue(context)
        
        return when (result) {
            is Iterable<*> -> result.joinToString(":") { it?.toString() ?: "null" }
            is Array<*> -> result.joinToString(":") { it?.toString() ?: "null" }
            else -> result?.toString() ?: "null"
        }
    }

    /**
     * 生成执行路径
     */
    private fun generateExecutionPath(method: Method): String {
        return "${method.declaringClass.simpleName}.${method.name}"
    }

    /**
     * 生成fallback key
     */
    private fun generateFallbackKey(joinPoint: ProceedingJoinPoint, prefix: String): String {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val className = method.declaringClass.simpleName
        val methodName = method.name
        
        return "$prefix:$className:$methodName"
    }

    /**
     * 生成hash值来缩短过长的key
     */
    private fun generateHash(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(input.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

}
