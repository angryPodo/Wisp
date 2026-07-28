package com.angrypodo.wisp.runtime.matcher

import com.angrypodo.wisp.runtime.spi.RouteFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WispRouteResolverTest {

    private val noopFactory = RouteFactory { it }

    private fun resolver(vararg patterns: String): WispRouteResolver {
        return WispRouteResolver(patterns.associateWith { noopFactory })
    }

    private fun matched(result: WispRouteResolver.Result): List<WispRouteResolver.Match> {
        assertTrue(
            result is WispRouteResolver.Result.Matched,
            "매칭에 성공해야 합니다: $result"
        )
        return (result as WispRouteResolver.Result.Matched).matches
    }

    @Test
    fun `단일 리터럴 라우트를 매칭한다`() {
        val result = resolver("home").resolve(listOf("home"))

        val matches = matched(result)
        assertEquals(1, matches.size)
        assertEquals("home", matches[0].pattern)
    }

    @Test
    fun `placeholder 패턴은 여러 세그먼트를 소비하고 경로 변수를 추출한다`() {
        val result = resolver("product/{productId}").resolve(listOf("product", "123"))

        val matches = matched(result)
        assertEquals(1, matches.size)
        assertEquals("product/{productId}", matches[0].pattern)
        assertEquals(mapOf("productId" to "123"), matches[0].pathVariables)
    }

    @Test
    fun `여러 라우트를 순서대로 매칭하여 백스택을 구성한다`() {
        val result = resolver("home", "product/{productId}", "user/{userId}")
            .resolve(listOf("home", "product", "123", "user", "99"))

        val matches = matched(result)
        assertEquals(
            listOf("home", "product/{productId}", "user/{userId}"),
            matches.map { it.pattern }
        )
        assertEquals(mapOf("productId" to "123"), matches[1].pathVariables)
        assertEquals(mapOf("userId" to "99"), matches[2].pathVariables)
    }

    @Test
    fun `탐욕적 매칭이 막히면 백트래킹하여 짧은 패턴을 시도한다`() {
        // If "product/{productId}" consumes ["product", "user"], "99" cannot match.
        // After backtracking it must match as "product" (1 segment) + "user/{userId}".
        val result = resolver("product", "product/{productId}", "user/{userId}")
            .resolve(listOf("product", "user", "99"))

        val matches = matched(result)
        assertEquals(listOf("product", "user/{userId}"), matches.map { it.pattern })
        assertEquals(mapOf("userId" to "99"), matches[1].pathVariables)
    }

    @Test
    fun `같은 길이면 리터럴 패턴을 placeholder 패턴보다 우선한다`() {
        val result = resolver("shop/cart", "shop/{page}").resolve(listOf("shop", "cart"))

        val matches = matched(result)
        assertEquals(listOf("shop/cart"), matches.map { it.pattern })
    }

    @Test
    fun `더 긴 패턴을 짧은 패턴보다 우선한다`() {
        val result = resolver("product", "product/{productId}")
            .resolve(listOf("product", "123"))

        val matches = matched(result)
        assertEquals(listOf("product/{productId}"), matches.map { it.pattern })
        assertEquals(mapOf("productId" to "123"), matches[0].pathVariables)
    }

    @Test
    fun `리터럴 매칭은 대소문자를 구분하지 않는다`() {
        val result = resolver("mypage").resolve(listOf("MyPage"))

        assertEquals(1, matched(result).size)
    }

    @Test
    fun `등록되지 않은 세그먼트를 만나면 실패 지점을 보고한다`() {
        val result = resolver("home").resolve(listOf("home", "unknown"))

        assertTrue(result is WispRouteResolver.Result.Unmatched)
        assertEquals("unknown", (result as WispRouteResolver.Result.Unmatched).segment)
    }

    @Test
    fun `중간에 placeholder 값이 빠지면 가장 깊은 실패 지점을 보고한다`() {
        val result = resolver("home", "product/{productId}")
            .resolve(listOf("home", "product"))

        assertTrue(result is WispRouteResolver.Result.Unmatched)
        assertEquals("product", (result as WispRouteResolver.Result.Unmatched).segment)
    }
}
