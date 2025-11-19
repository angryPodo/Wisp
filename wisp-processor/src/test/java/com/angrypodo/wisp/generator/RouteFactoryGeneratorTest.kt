package com.angrypodo.wisp.generator

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.angrypodo.wisp.model.ParameterInfo
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class RouteFactoryGeneratorTest {

    private lateinit var logger: TestLogger
    private lateinit var generator: RouteFactoryGenerator

    @BeforeEach
    fun setUp() {
        logger = TestLogger()
        generator = RouteFactoryGenerator(logger)
    }

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
    @DisplayName("NonNull Double 파라미터가 있는 라우트의 팩토리를 생성한다")
    fun `generate_route_with_non_nullable_double`() {
        // Given: Non-nullable Double 파라미터가 있는 라우트 정보
        val routeInfo = ClassRouteInfo(
            routeClassName = ClassName("com.example", "Product"),
            factoryClassName = ClassName("com.example", "ProductRouteFactory"),
            parameters = listOf(
                ParameterInfo("price", DOUBLE, isNullable = false, isEnum = false)
            ),
            wispPath = "product/{price}"
        )

        // When: 코드를 생성하면
        val fileSpec = generator.generate(routeInfo)
        val generatedCode = fileSpec.toString()

        // Then: toDoubleOrNull()과 null 체크 및 예외 발생 코드가 포함되어야 한다
        assertTrue(generatedCode.contains("params[\"price\"]?.toDoubleOrNull()"))
        assertTrue(
            generatedCode.contains(
                "?: throw WispError.InvalidParameter(\"product/{price}\", \"price\")"
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
                ),
                ParameterInfo("rating", FLOAT, isNullable = false, isEnum = false)
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
        assertTrue(generatedCode.contains("params[\"rating\"]?.toFloatOrNull()"))
        val expectedConstructor = "return Article(articleId = articleId, " +
            "isFeatured = isFeatured, rating = rating)"
        assertTrue(generatedCode.contains(expectedConstructor))
    }

    @Test
    @DisplayName("지원하지 않는 타입의 파라미터가 있으면 KSPLogger로 에러를 기록한다")
    fun `log_error_for_unsupported_type`() {
        // Given: 지원하지 않는 타입의 파라미터 정보
        val unsupportedType = ClassName("java.util", "Date")
        val routeInfo = ClassRouteInfo(
            routeClassName = ClassName("com.example", "Event"),
            factoryClassName = ClassName("com.example", "EventRouteFactory"),
            parameters = listOf(
                ParameterInfo("eventDate", unsupportedType, isNullable = false, isEnum = false)
            ),
            wispPath = "event/{eventDate}"
        )

        // When: 코드를 생성하면
        generator.generate(routeInfo)

        // Then: KSPLogger.error()가 호출되어야 한다
        assertEquals(1, logger.errorMessages.size)
        assertEquals(
            "Wisp Error: Unsupported type 'java.util.Date' for parameter 'eventDate'.",
            logger.errorMessages.first()
        )
    }
}

class TestLogger : KSPLogger {
    val errorMessages = mutableListOf<String>()
    val warningMessages = mutableListOf<String>()
    val infoMessages = mutableListOf<String>()

    override fun error(message: String, symbol: KSNode?) {
        errorMessages.add(message)
    }

    override fun info(message: String, symbol: KSNode?) {
        infoMessages.add(message)
    }

    override fun logging(message: String, symbol: KSNode?) {
        infoMessages.add(message)
    }

    override fun warn(message: String, symbol: KSNode?) {
        warningMessages.add(message)
    }

    override fun exception(e: Throwable) {
        // Not used
    }
}
