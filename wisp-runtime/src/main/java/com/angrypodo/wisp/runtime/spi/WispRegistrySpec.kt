package com.angrypodo.wisp.runtime.spi

interface WispRegistrySpec {
    fun createRoute(path: String): Any?
}
