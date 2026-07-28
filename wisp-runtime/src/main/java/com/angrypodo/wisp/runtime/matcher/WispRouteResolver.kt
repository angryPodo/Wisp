package com.angrypodo.wisp.runtime.matcher

import com.angrypodo.wisp.runtime.spi.RouteFactory

/**
 * Matches URI path segments against the registered route patterns to build
 * the backstack composition.
 *
 * A single pattern may consume multiple segments
 * (e.g. "product/{productId}" consumes the two segments "product/123").
 * At each position the most specific pattern (segment count, then literal
 * count, descending) is tried first; when a later match fails, the resolver
 * backtracks and tries the remaining candidates.
 */
internal class WispRouteResolver(routes: Map<String, RouteFactory>) {

    data class Match(
        val pattern: String,
        val factory: RouteFactory,
        val pathVariables: Map<String, String>
    )

    sealed interface Result {
        data class Matched(val matches: List<Match>) : Result

        /** [segment] is the segment at the deepest position where matching failed. */
        data class Unmatched(val segment: String) : Result
    }

    private class Candidate(val pattern: RoutePattern, val factory: RouteFactory)

    private val candidatesByHead: Map<String, List<Candidate>> =
        routes
            .map { (pattern, factory) -> Candidate(RoutePattern.parse(pattern), factory) }
            .groupBy { it.pattern.head }
            .mapValues { (_, candidates) ->
                candidates.sortedWith(
                    compareByDescending<Candidate> { it.pattern.size }
                        .thenByDescending { it.pattern.literalCount }
                )
            }

    fun resolve(pathSegments: List<String>): Result {
        val matches = mutableListOf<Match>()
        var deepestFailure = 0

        fun dfs(pos: Int): Boolean {
            if (pos == pathSegments.size) return true
            if (pos > deepestFailure) deepestFailure = pos

            val candidates = candidatesByHead[pathSegments[pos].lowercase()] ?: return false
            for (candidate in candidates) {
                val variables = candidate.pattern.matchAt(pathSegments, pos) ?: continue

                matches += Match(candidate.pattern.pattern, candidate.factory, variables)
                if (dfs(pos + candidate.pattern.size)) return true
                matches.removeAt(matches.lastIndex)
            }
            return false
        }

        return if (dfs(0)) {
            Result.Matched(matches.toList())
        } else {
            Result.Unmatched(pathSegments[deepestFailure])
        }
    }
}
