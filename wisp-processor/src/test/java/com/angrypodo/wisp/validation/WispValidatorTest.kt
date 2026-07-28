package com.angrypodo.wisp.validation

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.angrypodo.wisp.model.ParameterInfo
import com.angrypodo.wisp.model.RouteInfo
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WispValidatorTest {

    // --- validateDuplicatePaths ---

    @Test
    fun `validateDuplicatePaths returns Success when there are no duplicates`() {
        val routes = listOf(
            objectRoute("home", "HomeScreen"),
            objectRoute("profile", "ProfileScreen"),
            objectRoute("settings", "SettingsScreen")
        )

        val result = WispValidator.validateDuplicatePaths(routes)

        assertTrue(result is WispValidator.ValidationResult.Success)
    }

    @Test
    fun `validateDuplicatePaths returns Failure when there are duplicates`() {
        val routes = listOf(
            objectRoute("home", "HomeScreen"),
            objectRoute("home", "AnotherHomeScreen")
        )

        val result = WispValidator.validateDuplicatePaths(routes)

        assertTrue(result is WispValidator.ValidationResult.Failure)
        val failure = result as WispValidator.ValidationResult.Failure
        assertEquals(1, failure.errors.size)
        assertTrue(failure.errors[0].contains("path 'home' is already used by multiple routes"))
    }

    @Test
    fun `validateDuplicatePaths treats placeholder-name-only differences as duplicates`() {
        val routes = listOf(
            classRoute("product/{a}", "ProductA", listOf("a")),
            classRoute("product/{b}", "ProductB", listOf("b"))
        )

        val result = WispValidator.validateDuplicatePaths(routes)

        assertTrue(result is WispValidator.ValidationResult.Failure)
    }

    // --- validateRoute ---

    @Test
    fun `validateRoute accepts literal path on object route`() {
        val result = WispValidator.validateRoute(objectRoute("home", "HomeScreen"))

        assertTrue(result is WispValidator.ValidationResult.Success)
    }

    @Test
    fun `validateRoute accepts placeholder matching a constructor property`() {
        val route = classRoute("product/{productId}", "ProductDetail", listOf("productId"))

        val result = WispValidator.validateRoute(route)

        assertTrue(result is WispValidator.ValidationResult.Success)
    }

    @Test
    fun `validateRoute rejects blank path`() {
        val result = WispValidator.validateRoute(objectRoute("  ", "HomeScreen"))

        assertTrue(result is WispValidator.ValidationResult.Failure)
    }

    @Test
    fun `validateRoute rejects leading or trailing slash`() {
        val result = WispValidator.validateRoute(objectRoute("/home", "HomeScreen"))

        assertTrue(result is WispValidator.ValidationResult.Failure)
    }

    @Test
    fun `validateRoute rejects placeholder as first segment`() {
        val route = classRoute("{id}/detail", "Detail", listOf("id"))

        val result = WispValidator.validateRoute(route)

        assertTrue(result is WispValidator.ValidationResult.Failure)
    }

    @Test
    fun `validateRoute rejects duplicate placeholder names`() {
        val route = classRoute("product/{id}/{id}", "Detail", listOf("id"))

        val result = WispValidator.validateRoute(route)

        assertTrue(result is WispValidator.ValidationResult.Failure)
    }

    @Test
    fun `validateRoute rejects malformed placeholder segment`() {
        val route = classRoute("product/a{id}", "Detail", listOf("id"))

        val result = WispValidator.validateRoute(route)

        assertTrue(result is WispValidator.ValidationResult.Failure)
    }

    @Test
    fun `validateRoute rejects placeholder without matching constructor property`() {
        val route = classRoute("product/{productId}", "ProductDetail", listOf("otherName"))

        val result = WispValidator.validateRoute(route)

        assertTrue(result is WispValidator.ValidationResult.Failure)
        val failure = result as WispValidator.ValidationResult.Failure
        assertTrue(failure.errors[0].contains("no matching constructor property"))
    }

    @Test
    fun `validateRoute rejects placeholder on object route`() {
        val result = WispValidator.validateRoute(objectRoute("product/{id}", "ProductScreen"))

        assertTrue(result is WispValidator.ValidationResult.Failure)
    }

    // --- fixtures ---

    private fun objectRoute(path: String, className: String): RouteInfo {
        return ObjectRouteInfo(
            routeClassName = ClassName("com.example", className),
            factoryClassName = ClassName("com.example", "${className}Factory"),
            wispPath = path
        )
    }

    private fun classRoute(
        path: String,
        className: String,
        parameterNames: List<String>
    ): RouteInfo {
        return ClassRouteInfo(
            routeClassName = ClassName("com.example", className),
            factoryClassName = ClassName("com.example", "${className}Factory"),
            wispPath = path,
            parameters = parameterNames.map { name ->
                ParameterInfo(
                    name = name,
                    typeName = INT,
                    isNullable = false,
                    isEnum = false,
                    hasDefault = false
                )
            }
        )
    }
}
