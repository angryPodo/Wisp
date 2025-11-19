package com.angrypodo.wisp.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * 라우트 정보를 타입에 따라 분리하여 표현하는 Sealed Interface 입니다.
 */
sealed interface RouteInfo {
    val routeClassName: ClassName
    val factoryClassName: ClassName
    val wispPath: String
}

/**
 * 파라미터가 없는 object/data object 타입 라우트의 정보입니다.
 */
internal data class ObjectRouteInfo(
    override val routeClassName: ClassName,
    override val factoryClassName: ClassName,
    override val wispPath: String
) : RouteInfo

/**
 * 생성자 파라미터가 있는 class/data class 타입 라우트의 정보입니다.
 */
internal data class ClassRouteInfo(
    override val routeClassName: ClassName,
    override val factoryClassName: ClassName,
    override val wispPath: String,
    val parameters: List<ParameterInfo>
) : RouteInfo

/**
 * 라우트 생성자 파라미터 정보를 표현하는 데이터 클래스입니다.
 */
internal data class ParameterInfo(
    val name: String,
    val typeName: TypeName,
    val isNullable: Boolean,
    val isEnum: Boolean
)
