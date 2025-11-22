package com.angrypodo.wisp

import com.angrypodo.wisp.model.RouteInfo

internal object WispValidator {
    sealed interface ValidationResult {
        data object Success : ValidationResult
        data class Failure(val errors: List<String>) : ValidationResult
    }

    fun validate(routeInfo: RouteClassInfo): ValidationResult {
        if (!routeInfo.isSerializable()) {
            return ValidationResult.Failure(
                listOf(
                    "Wisp Error: Route Class '${routeInfo.qualifiedName}' " +
                        "must be annotated with @Serializable."
                )
            )
        }

        return ValidationResult.Success
    }

    fun validateDuplicatePaths(routes: List<RouteInfo>): ValidationResult {
        val duplicates = routes.groupBy { it.wispPath }
            .filter { it.value.size > 1 }

        if (duplicates.isEmpty()) return ValidationResult.Success

        val errorMessages = duplicates.map { (path, routeInfos) ->
            val conflictingClasses = routeInfos.joinToString(", ") { it.routeClassName.simpleName }
            "Wisp Error: The path '$path' is already used by multiple routes: [$conflictingClasses]"
        }

        return ValidationResult.Failure(errorMessages)
    }
}
