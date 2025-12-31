package com.angrypodo.wisp.runtime.spi

interface WispModuleRegistry {
    fun getRoutes(): Map<String, RouteFactory>
}
