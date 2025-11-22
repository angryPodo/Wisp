package com.angrypodo.wisp.generator

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.squareup.kotlinpoet.ClassName
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class RegistryGeneratorTest {

    @Test
    @DisplayName("RouteInfo를 받아 WispRegistry 오브젝트와 맵을 생성한다")
    fun `generate_registry_with_multiple_routes`() {
        // Given: RouteInfo 데이터 2개
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

        // When: 코드 생성 실행
        val fileSpec = WispRegistryGenerator.generate(routes)
        val generatedCode = fileSpec.toString()

        println(generatedCode)

        // Then: 생성된 WispRegistry 객체를 반환
        assertTrue(generatedCode.contains("object WispRegistry"))
        assertTrue(generatedCode.contains("val factories: Map<String, RouteFactory> = mapOf("))

        assertTrue(generatedCode.contains("import com.example.HomeRouteFactory"))
        assertTrue(generatedCode.contains("\"home\" to HomeRouteFactory"))

        assertTrue(generatedCode.contains("import com.example.ProfileRouteFactory"))
        assertTrue(generatedCode.contains("\"profile/{id}\" to ProfileRouteFactory"))
    }
}
