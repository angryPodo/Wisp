package com.angrypodo.wisp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * WispValidator의 순수 유효성 검증 로직을 테스트합니다.
 */
class WispValidatorTest {
    @Test
    fun `Serializable 어노테이션이 없으면 Failure를 반환한다`() {
        // Given
        val testInfo = RouteClassInfo(
            qualifiedName = "com.example.TestRoute",
            simpleName = "TestRoute",
            annotations = emptyList()
        )
        val expectedMessage = "Wisp Error: Route Class '${testInfo.qualifiedName}' " +
            "must be annotated with @Serializable."

        // When
        val result = WispValidator.validate(testInfo)

        // Then
        assertTrue(result is WispValidator.ValidationResult.Failure) {
            "결과 타입은 Failure여야 합니다."
        }
        assertEquals(
            listOf(expectedMessage),
            (result as WispValidator.ValidationResult.Failure).errors
        )
    }

    @Test
    fun `Serializable 어노테이션이 있으면 Success를 반환한다`() {
        // Given
        val serializableAnnotation = AnnotationInfo(
            qualifiedName = "kotlinx.serialization.Serializable",
            shortName = "Serializable"
        )
        val testInfo = RouteClassInfo(
            qualifiedName = "com.example.TestRoute",
            simpleName = "TestRoute",
            annotations = listOf(serializableAnnotation)
        )
        val expectedResult = WispValidator.ValidationResult.Success

        // When
        val result = WispValidator.validate(testInfo)

        // Then
        assertEquals(expectedResult, result)
    }
}
