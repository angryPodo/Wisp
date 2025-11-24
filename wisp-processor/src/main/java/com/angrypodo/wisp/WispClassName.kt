package com.angrypodo.wisp

import com.squareup.kotlinpoet.ClassName

internal object WispClassName {
    private const val RUNTIME_PACKAGE = "com.angrypodo.wisp.runtime"
    const val GENERATED_PACKAGE = "com.angrypodo.wisp.generated"

    val ROUTE_FACTORY = ClassName(RUNTIME_PACKAGE, "RouteFactory")
    val WISP_REGISTRY_SPEC = ClassName(RUNTIME_PACKAGE, "WispRegistrySpec")
    val WISP_URI_MATCHER = ClassName(RUNTIME_PACKAGE, "WispUriMatcher")

    val UNKNOWN_PATH_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "UnknownPath")
    val MISSING_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "MissingParameter")
    val INVALID_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "InvalidParameter")
}
