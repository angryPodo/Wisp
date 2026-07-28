package com.angrypodo.wisp.runtime

/**
 * Runtime errors produced by the Wisp library.
 * Errors are never thrown; they are delivered via [WispResult.Failure] and the onError callback.
 */
sealed class WispError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    class ParsingFailed(uri: String, reason: String) :
        WispError("Failed to parse URI: $uri. Reason: $reason")

    class UnknownPath(val segment: String) :
        WispError("The path segment \"$segment\" is not registered with any @Wisp annotation.")

    class MissingParameter(pattern: String, paramNames: String) :
        WispError("Required parameter(s) \"$paramNames\" missing for route \"$pattern\".")

    class InvalidParameter(pattern: String, detail: String?) :
        WispError("Failed to create route for \"$pattern\". Detail: $detail")

    class NavigationFailed(cause: Throwable) :
        WispError(
            "Navigation failed: ${cause::class.simpleName ?: "Unknown"}. Detail: ${cause.message}",
            cause
        )
}
