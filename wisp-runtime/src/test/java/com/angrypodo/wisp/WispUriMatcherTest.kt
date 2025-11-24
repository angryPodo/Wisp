package com.angrypodo.wisp

import com.angrypodo.wisp.runtime.WispUriMatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class WispUriMatcherTest {
    @Test
    @DisplayName("고정 경로가 정확히 일치하면 빈 맵을 반환한다")
    fun matchExactPath() {
        // Given
        val inputUri = "home/dashboard"
        val routePattern = "home/dashboard"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty(), "파라미터가 없는 고정 경로는 빈 맵을 반환해야 합니다.")
    }

    @Test
    @DisplayName("Path Variable이 포함된 경우 해당 값을 정확히 추출한다")
    fun matchPathVariable() {
        // Given
        val inputUri = "profile/12345"
        val routePattern = "profile/{userId}"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNotNull(result)
        assertEquals("12345", result["userId"])
    }

    @Test
    @DisplayName("여러 개의 경로 변수를 각각 올바른 키로 추출한다")
    fun matchMultiplePathVariables() {
        // Given
        val inputUri = "shop/category/books/item/99"
        val routePattern = "shop/category/{categoryName}/item/{itemId}"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNotNull(result)
        assertEquals("books", result["categoryName"])
        assertEquals("99", result["itemId"])
    }

    @Test
    @DisplayName("Query Parameter가 포함된 경우 함께 추출한다")
    fun matchQueryParameters() {
        // Given
        val inputUri = "search?keyword=kotlin&sort=latest"
        val routePattern = "search"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNotNull(result)
        assertEquals("kotlin", result["keyword"])
        assertEquals("latest", result["sort"])
    }

    @Test
    @DisplayName("경로 변수와 쿼리 파라미터가 섞여 있다면 모두 추출하여 병합한다")
    fun matchMixedParameters() {
        // Given
        val inputUri = "profile/user_123?ref=share_button&mode=dark"
        val routePattern = "profile/{id}"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNotNull(result)
        assertEquals("user_123", result["id"])
        assertEquals("share_button", result["ref"])
        assertEquals("dark", result["mode"])
    }

    @Test
    @DisplayName("경로 세그먼트의 개수가 다르면 매칭에 실패한다")
    fun failWhenSegmentCountDiffers() {
        // Given
        val inputUri = "profile/123/edit"
        val routePattern = "profile/{id}"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNull(result, "세그먼트 길이가 다르면 null을 반환해야 합니다.")
    }

    @Test
    @DisplayName("고정 경로 부분이 다르면 매칭에 실패한다")
    fun failWhenStaticPathDiffers() {
        // Given
        val inputUri = "settings/profile"
        val routePattern = "settings/account"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNull(result)
    }

    @Test
    @DisplayName("경로 매칭 시 대소문자를 구분하지 않는다")
    fun matchIgnoreCase() {
        // Given
        val inputUri = "MyPage/Settings"
        val routePattern = "mypage/settings"

        // When
        val result = WispUriMatcher.match(inputUri, routePattern)

        // Then
        assertNotNull(result, "대소문자가 달라도 문자가 같으면 매칭되어야 합니다.")
    }
}
