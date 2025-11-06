package com.angrypodo.wisp.annotations

/**
 * Jetpack Compose Navigation의 @Serializable 라우트를
 * 딥링크 대상으로 지정하는 어노테이션입니다.
 *
 * @param path 이 라우트와 매핑될 URI 경로 템플릿입니다.
 *             경로 파라미터는 {placeholder} 형식으로 지정할 수 있습니다.
 *             (예: "profile/{userId}")
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Wisp(
    val path: String
)
