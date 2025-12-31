package com.angrypodo.wisp.util

import com.squareup.kotlinpoet.ClassName

internal object WispClassName {
    private const val RUNTIME_PACKAGE = "com.angrypodo.wisp.runtime"
    const val GENERATED_PACKAGE = "com.angrypodo.wisp.generated"

    val ROUTE_FACTORY = ClassName("com.angrypodo.wisp.runtime.spi", "RouteFactory")
    val WISP_MODULE_REGISTRY = ClassName("com.angrypodo.wisp.runtime.spi", "WispModuleRegistry")

    val UNKNOWN_PATH_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "UnknownPath")
    val MISSING_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "MissingParameter")
    val INVALID_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "InvalidParameter")
}
