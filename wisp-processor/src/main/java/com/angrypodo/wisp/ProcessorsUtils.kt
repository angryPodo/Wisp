package com.angrypodo.wisp

private const val SERIALIZABLE_SHORT_NAME = "Serializable"
private const val SERIALIZABLE_ANNOTATION = "kotlinx.serialization.Serializable"

/**
 * RouteClassInfo가 @Serializable 어노테이션을 가지고 있는지 검사하는 함수입니다.
 */
internal fun RouteClassInfo.isSerializable(): Boolean = annotations.any {
    it.shortName == SERIALIZABLE_SHORT_NAME && it.qualifiedName == SERIALIZABLE_ANNOTATION
}
