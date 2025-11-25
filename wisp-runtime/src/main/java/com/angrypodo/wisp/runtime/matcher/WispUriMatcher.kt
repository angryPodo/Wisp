package com.angrypodo.wisp.runtime.matcher

object WispUriMatcher {

    /**
     * 입력된 URI와 라우트 패턴을 비교하여 매칭 여부를 확인하고 파라미터를 추출합니다.
     * 매칭 실패 시 null을 반환합니다.
     *
     * @param inputUri 비교할 실제 URI (예: "profile/123?ref=share")
     * @param routePattern 라우트 템플릿 (예: "profile/{id}")
     */
    fun match(inputUri: String, routePattern: String): Map<String, String>? {
        val path = inputUri.substringBefore('?')
        val query = inputUri.substringAfter('?', missingDelimiterValue = "")

        val pathSegments = path.split('/')
        val patternSegments = routePattern.split('/')

        if (pathSegments.size != patternSegments.size) return null

        val params = mutableMapOf<String, String>()
        if (!matchPathSegments(params, pathSegments, patternSegments)) {
            return null
        }

        if (query.isNotEmpty()) {
            parseQueryString(query, params)
        }

        return params
    }

    private fun matchPathSegments(
        params: MutableMap<String, String>,
        pathSegments: List<String>,
        patternSegments: List<String>
    ): Boolean {
        for (i in patternSegments.indices) {
            val patternSegment = patternSegments[i]
            val pathSegment = pathSegments[i]

            if (isPlaceholder(patternSegment)) {
                val key = patternSegment.removeSurrounding("{", "}")
                params[key] = pathSegment
            } else if (!patternSegment.equals(pathSegment, ignoreCase = true)) {
                return false
            }
        }
        return true
    }

    private fun isPlaceholder(segment: String): Boolean =
        segment.startsWith("{") && segment.endsWith("}")

    private fun parseQueryString(query: String, params: MutableMap<String, String>) {
        query.split('&').forEach { pair ->
            val parts = pair.split('=', limit = 2)

            if (parts.size == 2) params[parts[0]] = parts[1]
        }
    }
}
