package com.angrypodo.wisp.runtime

interface WispRegistrySpec {
    fun getRouteFactory(routePattern: String): RouteFactory?
    fun getPatterns(): Set<String>
}
