package com.angrypodo.wisp.runtime

import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private const val STACK = "stack"

class DefaultWispUriParser : WispUriParser {
    override fun parse(uri: Uri): List<String> {
        val encodedStack = uri.getQueryParameter(STACK)

        if (encodedStack.isNullOrBlank()) {
            throw WispError.ParsingFailed(uri.toString(), "Missing 'stack' query parameter")
        }

        return try {
            val decodedStack = URLDecoder.decode(encodedStack, StandardCharsets.UTF_8.name())

            decodedStack.split("|").filter { it.isNotBlank() }
        } catch (e: Exception) {
            throw WispError.ParsingFailed(uri.toString(), e.message ?: "Unknown decoding error")
        }
    }
}
