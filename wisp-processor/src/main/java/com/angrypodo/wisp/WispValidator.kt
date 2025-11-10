package com.angrypodo.wisp

internal object WispValidator {
    sealed interface ValidationResult {
        data object Success : ValidationResult
        data class Failure(val message: String) : ValidationResult
    }

    fun validate(routeInfo: RouteClassInfo): ValidationResult {
        if (!routeInfo.isSerializable()) {
            return ValidationResult.Failure(
                message = "Wisp Error: Route Class '${routeInfo.qualifiedName}' must be annotated with @Serializable."
            )
        }

        return ValidationResult.Success
    }
}
