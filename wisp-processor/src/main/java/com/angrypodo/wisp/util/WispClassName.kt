package com.angrypodo.wisp.util

import com.squareup.kotlinpoet.ClassName

internal object WispClassName {
    private const val RUNTIME_PACKAGE = "com.angrypodo.wisp.runtime"
    const val GENERATED_PACKAGE = "com.angrypodo.wisp.generated"

    val ROUTE_FACTORY = ClassName("com.angrypodo.wisp.runtime.spi", "RouteFactory")
    val WISP_REGISTRY_SPEC = ClassName("com.angrypodo.wisp.runtime.spi", "WispRegistrySpec")
    val WISP_URI_MATCHER = ClassName("com.angrypodo.wisp.runtime.matcher", "WispUriMatcher")

    val UNKNOWN_PATH_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "UnknownPath")
    val MISSING_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "MissingParameter")
    val INVALID_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "InvalidParameter")
}
