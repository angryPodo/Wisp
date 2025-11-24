package com.angrypodo.wisp.runtime

/**
 * Wisp 라이브러리에서 발생하는 런타임 에러를 정의하는 Sealed Class 입니다.
 */
sealed class WispError(override val message: String) : Exception(message) {
    class MissingParameter(path: String, paramName: String) :
        WispError("Required parameter \"$paramName\" is missing in path \"$path\".")

    class InvalidParameter(path: String, paramName: String) :
        WispError("Parameter \"$paramName\" in path \"$path\" could not be converted.")

    class ParsingFailed(uri: String, reason: String) :
        WispError("Failed to parse URI: $uri. Reason: $reason")

    class UnknownPath(path: String) :
        WispError("The path \"$path\" is not registered with any @Wisp annotation.")

    class NavigationFailed(reason: String, detail: String?) :
        WispError("Navigation failed: $reason. Detail: $detail")
}
