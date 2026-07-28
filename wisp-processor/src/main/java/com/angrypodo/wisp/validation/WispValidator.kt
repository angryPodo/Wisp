package com.angrypodo.wisp.validation

import com.angrypodo.wisp.model.ClassRouteInfo
import com.angrypodo.wisp.model.ObjectRouteInfo
import com.angrypodo.wisp.model.RouteInfo

internal object WispValidator {
    sealed interface ValidationResult {
        data object Success : ValidationResult
        data class Failure(val errors: List<String>) : ValidationResult
    }

    /**
     * Validates a single route's path template format and the mapping between
     * placeholders and constructor properties. Enforces the same rules as the
     * runtime's RoutePattern.parse, but at compile time.
     */
    fun validateRoute(route: RouteInfo): ValidationResult {
        val path = route.wispPath
        val className = route.routeClassName.simpleName

        if (path.isBlank()) {
            return ValidationResult.Failure(
                listOf("Wisp Error: Route '$className' has a blank @Wisp path.")
            )
        }

        val errors = mutableListOf<String>()
        val segments = path.split('/')

        if (segments.any { it.isEmpty() }) {
            errors += "Wisp Error: Path '$path' on '$className' must not contain " +
                "leading/trailing or duplicate '/'."
        }

        val placeholders = collectPlaceholders(path, className, segments, errors)

        placeholders.groupBy { it }
            .filterValues { it.size > 1 }
            .keys
            .forEach { name ->
                errors += "Wisp Error: Path '$path' on '$className' declares " +
                    "the placeholder '{$name}' more than once."
            }

        validatePlaceholderProperties(route, path, className, placeholders, errors)

        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Failure(errors)
    }

    /**
     * Treats patterns that differ only by placeholder name
     * ('product/{a}' vs 'product/{b}') as conflicts as well.
     */
    fun validateDuplicatePaths(routes: List<RouteInfo>): ValidationResult {
        val duplicates = routes.groupBy { normalize(it.wispPath) }
            .filter { it.value.size > 1 }

        if (duplicates.isEmpty()) {
            return ValidationResult.Success
        }

        val errorMessages = duplicates.map { (_, routeInfos) ->
            val representativePath = routeInfos.first().wispPath
            val conflictingClasses = routeInfos.joinToString(", ") { it.routeClassName.simpleName }
            "Wisp Error: The path '$representativePath' is already used by multiple routes: " +
                "[$conflictingClasses]"
        }

        return ValidationResult.Failure(errorMessages)
    }

    private fun collectPlaceholders(
        path: String,
        className: String,
        segments: List<String>,
        errors: MutableList<String>
    ): List<String> {
        val placeholders = mutableListOf<String>()

        segments.filter { it.isNotEmpty() }.forEachIndexed { index, segment ->
            val isPlaceholder = segment.startsWith("{") && segment.endsWith("}")
            when {
                isPlaceholder -> {
                    val name = segment.substring(1, segment.length - 1)
                    if (name.isBlank()) {
                        errors += "Wisp Error: Path '$path' on '$className' contains " +
                            "an empty placeholder."
                    } else {
                        placeholders += name
                    }
                    if (index == 0) {
                        errors += "Wisp Error: Path '$path' on '$className' must start " +
                            "with a literal segment, not a placeholder."
                    }
                }

                segment.contains('{') || segment.contains('}') -> {
                    errors += "Wisp Error: Path '$path' on '$className' contains " +
                        "a malformed placeholder segment '$segment'."
                }
            }
        }

        return placeholders
    }

    private fun validatePlaceholderProperties(
        route: RouteInfo,
        path: String,
        className: String,
        placeholders: List<String>,
        errors: MutableList<String>
    ) {
        when (route) {
            is ObjectRouteInfo -> {
                if (placeholders.isNotEmpty()) {
                    errors += "Wisp Error: Object route '$className' cannot declare " +
                        "path parameters in '$path'."
                }
            }

            is ClassRouteInfo -> {
                val propertyNames = route.parameters.map { it.name }.toSet()
                placeholders.filterNot { it in propertyNames }.forEach { name ->
                    errors += "Wisp Error: Placeholder '{$name}' in path '$path' has no " +
                        "matching constructor property in '$className'."
                }
            }
        }
    }

    private fun normalize(path: String): String {
        return path.split('/').joinToString("/") { segment ->
            if (segment.startsWith("{") && segment.endsWith("}")) "{}" else segment.lowercase()
        }
    }
}
