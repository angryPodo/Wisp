package com.angrypodo.wisp.runtime.spi

/**
 * Interface implemented by the KSP-generated factory classes.
 * Declared public because generated code in other modules must access it.
 */
fun interface RouteFactory {
    fun create(params: Map<String, String>): Any
}
