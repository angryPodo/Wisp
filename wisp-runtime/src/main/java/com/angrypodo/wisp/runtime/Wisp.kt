package com.angrypodo.wisp.runtime

import android.net.Uri

class Wisp(
    private val registry: WispRegistrySpec,
    private val parser: WispUriParser = DefaultWispUriParser()
) {

    fun resolveRoutes(uri: Uri): List<Any> {
        val inputUris = parser.parse(uri)

        return inputUris.map { inputUri ->
            createRouteObject(inputUri)
        }
    }

    private fun createRouteObject(inputUri: String): Any {
        val allPatterns = registry.getPatterns()

        for (pattern in allPatterns) {
            val params = WispUriMatcher.match(inputUri, pattern)

            if (params != null) {
                val factory = registry.getRouteFactory(pattern)
                    ?: throw WispError.UnknownPath(pattern)

                return factory.create(params)
            }
        }

        throw WispError.UnknownPath(inputUri)
    }
}
