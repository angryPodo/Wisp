package com.angrypodo.wisp.runtime.parser

import android.net.Uri

interface WispUriParser {
    /**
     * @param uri The received deep link Uri
     * @return List of path segments representing the backstack
     */
    fun parse(uri: Uri): List<String>
}
