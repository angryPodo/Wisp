package com.angrypodo.wisp

import com.squareup.kotlinpoet.ClassName

internal object WispClassName {
    private const val RUNTIME_PACKAGE = "com.angrypodo.wisp.runtime"
    const val GENERATED_PACKAGE = "com.angrypodo.wisp.generated"

    val ROUTE_FACTORY = ClassName(RUNTIME_PACKAGE, "RouteFactory")

    val MISSING_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "MissingParameter")
    val INVALID_PARAMETER_ERROR = ClassName(RUNTIME_PACKAGE, "WispError", "InvalidParameter")
}
