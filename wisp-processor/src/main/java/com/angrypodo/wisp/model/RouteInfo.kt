package com.angrypodo.wisp.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * Sealed interface that represents route information, split by route kind.
 */
sealed interface RouteInfo {
    val routeClassName: ClassName
    val factoryClassName: ClassName
    val wispPath: String
}

/**
 * Route information for parameterless object/data object routes.
 */
internal data class ObjectRouteInfo(
    override val routeClassName: ClassName,
    override val factoryClassName: ClassName,
    override val wispPath: String
) : RouteInfo

/**
 * Route information for class/data class routes with constructor parameters.
 */
internal data class ClassRouteInfo(
    override val routeClassName: ClassName,
    override val factoryClassName: ClassName,
    override val wispPath: String,
    val parameters: List<ParameterInfo>
) : RouteInfo

/**
 * Constructor parameter information of a route.
 */
internal data class ParameterInfo(
    val name: String,
    val typeName: TypeName,
    val isNullable: Boolean,
    val isEnum: Boolean,
    val hasDefault: Boolean
)
