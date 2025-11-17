package com.angrypodo.wisp.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * @Wisp 라우트의 생성에 필요한 정보를 담는 순수 Kotlin 데이터 클래스입니다.
 */
internal data class RouteInfo(
    val routeClassName: ClassName,
    val factoryClassName: ClassName,
    val parameters: List<ParameterInfo>,
    val wispPath: String
)

/**
 * 라우트 생성자 파라미터 정보를 표현하는 데이터 클래스입니다.
 */
internal data class ParameterInfo(
    val name: String,
    val typeName: TypeName,
    val isNullable: Boolean,
    val isEnum: Boolean
)
