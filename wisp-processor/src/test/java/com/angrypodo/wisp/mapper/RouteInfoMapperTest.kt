package com.angrypodo.wisp.mapper

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class RouteInfoMapperTest {

    @Test
    fun `toRouteInfo returns null if @Wisp annotation is missing`() {
        val classDeclaration = mock<KSClassDeclaration>()
        whenever(classDeclaration.annotations).thenReturn(emptySequence())

        val result = classDeclaration.toRouteInfo()

        assertNull(result)
    }

    @Test
    fun `toRouteInfo returns ObjectRouteInfo for object declaration`() {
        val mockPackageName = mock<KSName>()
        whenever(mockPackageName.asString()).thenReturn("com.example")

        val mockSimpleName = mock<KSName>()
        whenever(mockSimpleName.asString()).thenReturn("HomeScreen")

        val mockQualifiedName = mock<KSName>()
        whenever(mockQualifiedName.asString()).thenReturn("com.example.HomeScreen")

        val classDeclaration = mock<KSClassDeclaration>()
        whenever(classDeclaration.classKind).thenReturn(ClassKind.OBJECT)
        whenever(classDeclaration.packageName).thenReturn(mockPackageName)
        whenever(classDeclaration.simpleName).thenReturn(mockSimpleName)
        whenever(classDeclaration.qualifiedName).thenReturn(mockQualifiedName)

        val mockAnnotation = createMockWispAnnotation("home")
        whenever(classDeclaration.annotations).thenReturn(sequenceOf(mockAnnotation))

        val result = classDeclaration.toRouteInfo()

        assertNotNull(result)
        assertTrue(result is ObjectRouteInfo)
        assertEquals("home", result?.wispPath)
    }

    @Test
    fun `toRouteInfo returns ClassRouteInfo for class declaration`() {
        val mockPackageName = mock<KSName>()
        whenever(mockPackageName.asString()).thenReturn("com.example")

        val mockSimpleName = mock<KSName>()
        whenever(mockSimpleName.asString()).thenReturn("ProfileScreen")

        val mockQualifiedName = mock<KSName>()
        whenever(mockQualifiedName.asString()).thenReturn("com.example.ProfileScreen")

        val classDeclaration = mock<KSClassDeclaration>()
        whenever(classDeclaration.classKind).thenReturn(ClassKind.CLASS)
        whenever(classDeclaration.packageName).thenReturn(mockPackageName)
        whenever(classDeclaration.simpleName).thenReturn(mockSimpleName)
        whenever(classDeclaration.qualifiedName).thenReturn(mockQualifiedName)
        whenever(classDeclaration.primaryConstructor).thenReturn(null)

        val mockAnnotation = createMockWispAnnotation("profile")
        whenever(classDeclaration.annotations).thenReturn(sequenceOf(mockAnnotation))

        val result = classDeclaration.toRouteInfo()

        assertNotNull(result)
        assertTrue(result is ClassRouteInfo)
    }

    private fun createMockWispAnnotation(path: String): KSAnnotation {
        val annotation = mock<KSAnnotation>()

        val shortName = mock<KSName>()
        whenever(shortName.asString()).thenReturn("Wisp")
        whenever(annotation.shortName).thenReturn(shortName)

        val arg = mock<KSValueArgument>()
        val argName = mock<KSName>()
        whenever(argName.asString()).thenReturn("path")
        whenever(arg.name).thenReturn(argName)
        whenever(arg.value).thenReturn(path)

        whenever(annotation.arguments).thenReturn(listOf(arg))

        return annotation
    }
}
