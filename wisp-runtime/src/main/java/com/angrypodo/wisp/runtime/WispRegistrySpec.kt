package com.angrypodo.wisp.runtime

interface WispRegistrySpec {
    fun createRoute(path: String): Any?
}
