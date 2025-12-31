package com.angrypodo.wisp.generator

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.squareup.kotlinpoet.ClassName
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class RegistryGeneratorTest {

    @Test
    @DisplayName("Should generate WispModuleRegistry object and map from RouteInfo")
    fun `generate_registry_with_multiple_routes`() {
        // Given: Two RouteInfo objects
        val homeRoute = ObjectRouteInfo(
            routeClassName = ClassName("com.example", "Home"),
            factoryClassName = ClassName("com.example", "HomeRouteFactory"),
            wispPath = "home"
        )

        val profileRoute = ClassRouteInfo(
            routeClassName = ClassName("com.example", "Profile"),
            factoryClassName = ClassName("com.example", "ProfileRouteFactory"),
            wispPath = "profile/{id}",
            parameters = emptyList()
        )

        val routes = listOf(homeRoute, profileRoute)

        // When: Code generation is executed
        val generatedRegistry = WispRegistryGenerator().generate(routes)
        val generatedCode = generatedRegistry.fileSpec.toString()

        // Then: Return the generated WispModuleRegistry object (with a hash in its name)
        assertTrue(generatedCode.contains("class WispModuleRegistry_"))
        // Check for SPI interface implementation
        assertTrue(generatedCode.contains(": WispModuleRegistry"))
        // Check for map creation
        assertTrue(generatedCode.contains("private val factories:"))
        assertTrue(generatedCode.contains("Map<String, RouteFactory>"))
        assertTrue(generatedCode.contains("= mapOf("))
        // Check for getRoutes method
        assertTrue(generatedCode.contains("override fun getRoutes(): Map<String, RouteFactory>"))

        // Check for route factory mappings
        assertTrue(generatedCode.contains("import com.example.HomeRouteFactory"))
        assertTrue(generatedCode.contains("\"home\" to HomeRouteFactory"))

        assertTrue(generatedCode.contains("import com.example.ProfileRouteFactory"))
        assertTrue(generatedCode.contains("\"profile/{id}\" to ProfileRouteFactory"))
    }
}
