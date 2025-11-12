package com.angrypodo.wisp

/**
 * KSClassDeclaration을 대체하는 순수 Kotlin 모델입니다.
 */
internal data class RouteClassInfo(
    val qualifiedName: String?,
    val simpleName: String,
    val annotations: List<AnnotationInfo>
)

/**
 * KSAnnotation을 대체하는 순수 Kotlin 모델입니다.
 */
internal data class AnnotationInfo(
    val qualifiedName: String?,
    val shortName: String
)
