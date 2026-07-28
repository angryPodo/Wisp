package com.angrypodo.wisp.runtime

/**
 * Result of deep link handling. Wisp returns this type instead of throwing exceptions.
 */
sealed interface WispResult {
    /** [routes] is the list of route objects created in backstack order. */
    data class Success(val routes: List<Any>) : WispResult

    data class Failure(val error: WispError) : WispResult
}
