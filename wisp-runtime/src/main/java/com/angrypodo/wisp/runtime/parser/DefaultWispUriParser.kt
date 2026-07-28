package com.angrypodo.wisp.runtime.parser

import android.net.Uri

/**
 * Default URI parser implementation.
 * Splits the URI path by the given [delimiter] into a list of route path segments.
 *
 * e.g. "myapp://home/product" (delimiter="/") -> ["home", "product"]
 */
class DefaultWispUriParser(
    private val delimiter: String = "/"
) : WispUriParser {

    override fun parse(uri: Uri): List<String> {
        val path = uri.path ?: return emptyList()

        // Strip the leading delimiter if present (e.g. "/home" -> "home")
        val trimmedPath = if (path.startsWith(delimiter)) {
            path.substring(delimiter.length)
        } else {
            path
        }

        return trimmedPath.split(delimiter)
            .filter { it.isNotEmpty() }
    }
}
