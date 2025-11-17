package com.angrypodo.wisp.mapper

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.angrypodo.wisp.model.ParameterInfo
import com.angrypodo.wisp.model.RouteInfo
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

private const val WISP_SIMPLE_NAME = "Wisp"
private const val WISP_PATH_ARGUMENT = "path"

internal fun KSClassDeclaration.toRouteInfo(): RouteInfo? {
    val wispPath = getWispPath() ?: return null
    val routeClassName = toClassName()
    val factoryClassName = ClassName(packageName.asString(), "${simpleName.asString()}RouteFactory")

    if (classKind == ClassKind.OBJECT) {
        return ObjectRouteInfo(
            routeClassName = routeClassName,
            factoryClassName = factoryClassName,
            wispPath = wispPath
        )
    }

    return ClassRouteInfo(
        routeClassName = routeClassName,
        factoryClassName = factoryClassName,
        wispPath = wispPath,
        parameters = extractParameters()
    )
}

private fun KSClassDeclaration.getWispPath(): String? {
    val wispAnnotation = annotations.find { it.shortName.asString() == WISP_SIMPLE_NAME } ?: return null
    val pathArgument = wispAnnotation.arguments.firstOrNull { it.name?.asString() == WISP_PATH_ARGUMENT }
        ?: wispAnnotation.arguments.firstOrNull()
    return pathArgument?.value as? String
}

private fun KSClassDeclaration.extractParameters(): List<ParameterInfo> {
    return primaryConstructor?.parameters?.mapNotNull { parameter ->
        val parameterName = parameter.name?.asString() ?: return@mapNotNull null
        val resolvedType = parameter.type.resolve()
        val declaration = resolvedType.declaration
        val isEnum = declaration is KSClassDeclaration && declaration.classKind == ClassKind.ENUM_CLASS

        ParameterInfo(
            name = parameterName,
            typeName = resolvedType.toTypeName(),
            isNullable = resolvedType.isMarkedNullable,
            isEnum = isEnum
        )
    } ?: emptyList()
}
