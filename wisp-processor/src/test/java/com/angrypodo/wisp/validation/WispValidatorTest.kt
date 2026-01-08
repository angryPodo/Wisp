package com.angrypodo.wisp.validation

import com.angrypodo.wisp.model.ObjectRouteInfo
import com.squareup.kotlinpoet.ClassName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WispValidatorTest {

    @Test
    fun `validateDuplicatePaths returns Success when there are no duplicates`() {
        val routes = listOf(
            createMockRouteInfo("home", "HomeScreen"),
            createMockRouteInfo("profile", "ProfileScreen"),
            createMockRouteInfo("settings", "SettingsScreen")
        )

        val result = WispValidator.validateDuplicatePaths(routes)

        assertTrue(result is WispValidator.ValidationResult.Success)
    }

    @Test
    fun `validateDuplicatePaths returns Failure when there are duplicates`() {
        val routes = listOf(
            createMockRouteInfo("home", "HomeScreen"),
            createMockRouteInfo("home", "AnotherHomeScreen")
        )

        val result = WispValidator.validateDuplicatePaths(routes)

        assertTrue(result is WispValidator.ValidationResult.Failure)
        val failure = result as WispValidator.ValidationResult.Failure
        assertEquals(1, failure.errors.size)
        assertTrue(failure.errors[0].contains("path 'home' is already used by multiple routes"))
    }

    private fun createMockRouteInfo(path: String, className: String): ObjectRouteInfo {
        return ObjectRouteInfo(
            routeClassName = ClassName("com.example", className),
            factoryClassName = ClassName("com.example", "${className}Factory"),
            wispPath = path
        )
    }
}
