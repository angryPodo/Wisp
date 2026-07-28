package com.angrypodo.wisp.runtime.matcher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class RoutePatternTest {

    @Test
    fun `parse extracts size and literal count and head`() {
        val pattern = RoutePattern.parse("shop/category/{categoryName}/item/{itemId}")

        assertEquals(5, pattern.size)
        assertEquals(3, pattern.literalCount)
        assertEquals("shop", pattern.head)
    }

    @Test
    fun `matchAt extracts path variables`() {
        val pattern = RoutePattern.parse("profile/{userId}")

        val result = pattern.matchAt(listOf("profile", "12345"), 0)

        assertNotNull(result)
        assertEquals("12345", result["userId"])
    }

    @Test
    fun `matchAt matches literals case-insensitively`() {
        val pattern = RoutePattern.parse("mypage/settings")

        assertNotNull(pattern.matchAt(listOf("MyPage", "Settings"), 0))
    }

    @Test
    fun `matchAt fails when literal differs`() {
        val pattern = RoutePattern.parse("settings/account")

        assertNull(pattern.matchAt(listOf("settings", "profile"), 0))
    }

    @Test
    fun `matchAt fails when remaining segments are shorter than pattern`() {
        val pattern = RoutePattern.parse("profile/{userId}")

        assertNull(pattern.matchAt(listOf("profile"), 0))
    }

    @Test
    fun `matchAt matches from a middle position`() {
        val pattern = RoutePattern.parse("user/{userId}")

        val result = pattern.matchAt(listOf("home", "user", "99"), 1)

        assertNotNull(result)
        assertEquals("99", result["userId"])
    }

    @Test
    fun `parse rejects blank path`() {
        assertThrows(IllegalArgumentException::class.java) { RoutePattern.parse("  ") }
    }

    @Test
    fun `parse rejects leading slash`() {
        assertThrows(IllegalArgumentException::class.java) { RoutePattern.parse("/home") }
    }

    @Test
    fun `parse rejects empty segment`() {
        assertThrows(IllegalArgumentException::class.java) { RoutePattern.parse("home//detail") }
    }

    @Test
    fun `parse rejects placeholder as first segment`() {
        assertThrows(IllegalArgumentException::class.java) { RoutePattern.parse("{id}/detail") }
    }

    @Test
    fun `parse rejects duplicate placeholder names`() {
        assertThrows(IllegalArgumentException::class.java) {
            RoutePattern.parse("product/{id}/{id}")
        }
    }

    @Test
    fun `parse rejects malformed placeholder segment`() {
        assertThrows(IllegalArgumentException::class.java) { RoutePattern.parse("product/a{id}") }
    }

    @Test
    fun `parse rejects empty placeholder`() {
        assertThrows(IllegalArgumentException::class.java) { RoutePattern.parse("product/{}") }
    }
}
