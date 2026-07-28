package com.angrypodo.wisp.runtime.matcher

/**
 * Parsed form of a @Wisp path template.
 * e.g. "product/{productId}" -> [Literal("product"), Placeholder("productId")]
 */
internal class RoutePattern private constructor(
    val pattern: String,
    private val segments: List<Segment>
) {
    private sealed interface Segment {
        data class Literal(val value: String) : Segment
        data class Placeholder(val name: String) : Segment
    }

    /** Number of path segments this pattern consumes. */
    val size: Int = segments.size

    /** Number of literal segments, used to rank matching priority. */
    val literalCount: Int = segments.count { it is Segment.Literal }

    /** First literal segment (lowercase), used as the candidate lookup key. */
    val head: String = (segments.first() as Segment.Literal).value.lowercase()

    /**
     * Checks whether this pattern matches [pathSegments] starting at [pos].
     * Returns the extracted path variables on success, or null on failure.
     */
    fun matchAt(pathSegments: List<String>, pos: Int): Map<String, String>? {
        if (pos + size > pathSegments.size) return null

        val variables = mutableMapOf<String, String>()
        segments.forEachIndexed { index, segment ->
            val value = pathSegments[pos + index]
            when (segment) {
                is Segment.Literal ->
                    if (!segment.value.equals(value, ignoreCase = true)) return null

                is Segment.Placeholder -> variables[segment.name] = value
            }
        }
        return variables
    }

    companion object {
        /**
         * Parses a pattern string. The KSP processor enforces the same rules at
         * compile time, so a failure here is a developer error and throws immediately.
         */
        fun parse(pattern: String): RoutePattern {
            require(pattern.isNotBlank()) { "Wisp path must not be blank." }

            val rawSegments = pattern.split('/')
            require(rawSegments.none { it.isEmpty() }) {
                "Wisp path \"$pattern\" must not contain leading/trailing or duplicate '/'."
            }

            val segments = rawSegments.map { raw -> parseSegment(pattern, raw) }
            require(segments.first() is Segment.Literal) {
                "Wisp path \"$pattern\" must start with a literal segment."
            }

            val names = segments.filterIsInstance<Segment.Placeholder>().map { it.name }
            require(names.size == names.distinct().size) {
                "Wisp path \"$pattern\" contains duplicate placeholder names."
            }

            return RoutePattern(pattern, segments)
        }

        private fun parseSegment(pattern: String, raw: String): Segment {
            if (raw.startsWith("{") && raw.endsWith("}")) {
                val name = raw.substring(1, raw.length - 1)
                require(name.isNotBlank()) {
                    "Wisp path \"$pattern\" contains an empty placeholder."
                }
                return Segment.Placeholder(name)
            }
            require(!raw.contains('{') && !raw.contains('}')) {
                "Wisp path \"$pattern\" contains a malformed placeholder segment \"$raw\"."
            }
            return Segment.Literal(raw)
        }
    }
}
