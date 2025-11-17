package com.angrypodo.wisp.generator

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.angrypodo.wisp.model.ParameterInfo
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class RouteFactoryGeneratorTest {

    private val generator = RouteFactoryGenerator()

    @Test
    @DisplayName("파라미터가 없는 data object 라우트의 팩토리를 생성한다")
    fun `generate_no_parameter_route`() {
        // Given: 파라미터가 없는 data object 라우트 정보
        val routeInfo = ObjectRouteInfo(
            routeClassName = ClassName("com.example", "Home"),
            factoryClassName = ClassName("com.example", "HomeRouteFactory"),
            wispPath = "home"
        )

        // When: 코드를 생성하면
        val fileSpec = generator.generate(routeInfo)
        val generatedCode = fileSpec.toString()

        // Then: Expression body로 객체를 반환해야 한다
        assertTrue(generatedCode.contains("= Home"))
    }

    @Test
    @DisplayName("NonNull Int 파라미터가 있는 라우트의 팩토리를 생성한다")
    fun `generate_route_with_non_nullable_int`() {
        // Given: Non-nullable Int 파라미터가 있는 라우트 정보
        val routeInfo = ClassRouteInfo(
            routeClassName = ClassName("com.example", "Profile"),
            factoryClassName = ClassName("com.example", "ProfileRouteFactory"),
            parameters = listOf(
                ParameterInfo("userId", INT, isNullable = false, isEnum = false)
            ),
            wispPath = "profile/{userId}"
        )

        // When: 코드를 생성하면
        val fileSpec = generator.generate(routeInfo)
        val generatedCode = fileSpec.toString()

        // Then: toIntOrNull()과 null 체크 및 예외 발생 코드가 포함되어야 한다
        assertTrue(generatedCode.contains("params[\"userId\"]?.toIntOrNull()"))
        assertTrue(
            generatedCode.contains(
                "?: throw WispError.InvalidParameter(\"profile/{userId}\", \"userId\")"
            )
        )
    }

    @Test
    @DisplayName("Nullable String 파라미터가 있는 라우트의 팩토리를 생성한다")
    fun `generate_route_with_nullable_string`() {
        // Given: Nullable String 파라미터가 있는 라우트 정보
        val routeInfo = ClassRouteInfo(
            routeClassName = ClassName("com.example", "Search"),
            factoryClassName = ClassName("com.example", "SearchRouteFactory"),
            parameters = listOf(
                ParameterInfo(
                    "query",
                    STRING.copy(nullable = true),
                    isNullable = true,
                    isEnum = false
                )
            ),
            wispPath = "search"
        )

        // When: 코드를 생성하면
        val fileSpec = generator.generate(routeInfo)
        val generatedCode = fileSpec.toString()

        // Then: 단순히 파라미터를 가져오는 코드가 생성되어야 한다
        assertTrue(generatedCode.contains("val query = params[\"query\"]"))
        assertTrue(generatedCode.contains("return Search(query = query)"))
    }

    @Test
    @DisplayName("NonNull Enum 파라미터가 있는 라우트의 팩토리를 생성한다")
    fun `generate_route_with_non_nullable_enum`() {
        // Given: Non-nullable Enum 파라미터가 있는 라우트 정보
        val enumTypeName = ClassName("com.example", "ContentType")
        val routeInfo = ClassRouteInfo(
            routeClassName = ClassName("com.example", "Content"),
            factoryClassName = ClassName("com.example", "ContentRouteFactory"),
            parameters = listOf(
                ParameterInfo("type", enumTypeName, isNullable = false, isEnum = true)
            ),
            wispPath = "content/{type}"
        )

        // When: 코드를 생성하면
        val fileSpec = generator.generate(routeInfo)
        val generatedCode = fileSpec.toString()

        // Then: valueOf()와 uppercase()를 사용한 변환 코드가 포함되어야 한다
        assertTrue(
            generatedCode.contains(
                "runCatching { ContentType.valueOf(params[\"type\"]!!.uppercase()) }.getOrNull()"
            )
        )
        assertTrue(
            generatedCode.contains(
                "?: throw WispError.InvalidParameter(\"content/{type}\", \"type\")"
            )
        )
    }

    @Test
    @DisplayName("여러 타입의 파라미터를 가진 라우트의 팩토리를 생성한다")
    fun `generate_route_with_multiple_parameters`() {
        // Given: 여러 타입의 파라미터가 있는 라우트 정보
        val routeInfo = ClassRouteInfo(
            routeClassName = ClassName("com.example", "Article"),
            factoryClassName = ClassName("com.example", "ArticleRouteFactory"),
            parameters = listOf(
                ParameterInfo("articleId", LONG, isNullable = false, isEnum = false),
                ParameterInfo(
                    "isFeatured",
                    BOOLEAN.copy(nullable = true),
                    isNullable = true,
                    isEnum = false
                )
            ),
            wispPath = "article/{articleId}"
        )

        // When: 코드를 생성하면
        val fileSpec = generator.generate(routeInfo)
        val generatedCode = fileSpec.toString()

        // Then: 각 파라미터에 대한 변환 코드가 모두 포함되어야 한다
        assertTrue(generatedCode.contains("params[\"articleId\"]?.toLongOrNull()"))
        assertTrue(
            generatedCode.contains(
                "val isFeatured = params[\"isFeatured\"]?.toBooleanStrictOrNull()"
            )
        )
        assertTrue(
            generatedCode.contains("return Article(articleId = articleId, isFeatured = isFeatured)")
        )
    }
}
