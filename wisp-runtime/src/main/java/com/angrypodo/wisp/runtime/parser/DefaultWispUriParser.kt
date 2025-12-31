package com.angrypodo.wisp.runtime.parser

import android.net.Uri

/**
 * 기본 URI 파서 구현체입니다.
 * URI의 경로(Path)를 지정된 구분자([delimiter])로 분리하여 라우트 경로 리스트를 생성합니다.
 *
 * 예: "myapp://home/product" (delimiter="/") -> ["home", "product"]
 */
class DefaultWispUriParser(
    private val delimiter: String = "/"
) : WispUriParser {

    override fun parse(uri: Uri): List<String> {
        val path = uri.path ?: return emptyList()

        // 경로가 구분자로 시작하면 제거 (예: "/home" -> "home")
        val trimmedPath = if (path.startsWith(delimiter)) {
            path.substring(delimiter.length)
        } else {
            path
        }

        return trimmedPath.split(delimiter)
            .filter { it.isNotEmpty() }
    }
}
