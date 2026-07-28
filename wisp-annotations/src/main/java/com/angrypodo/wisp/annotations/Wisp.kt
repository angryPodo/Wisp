package com.angrypodo.wisp.annotations

/**
 * Marks a @Serializable route of Jetpack Compose Navigation
 * as a deep link destination.
 *
 * @param path The URI path template mapped to this route.
 *             Path parameters can be declared as {placeholder} segments.
 *             (e.g. "profile/{userId}")
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Wisp(
    val path: String
)
