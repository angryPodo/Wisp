package com.angrypodo.wisp.runtime

import android.net.Uri
import androidx.navigation.NavController

/**
 * End-user API that resolves a URI and immediately rebuilds the backstack.
 * Delegates all work to `Wisp.getDefaultInstance()`.
 *
 * @param uri Deep link URI
 * @return The result. Failures are returned as [WispResult.Failure]; nothing is thrown.
 * @throws IllegalStateException If `Wisp.initialize()` has not been called first
 */
fun NavController.navigateTo(uri: Uri): WispResult {
    return Wisp.getDefaultInstance().navigateTo(this, uri)
}

/**
 * Consumes a deep link previously held via `Wisp.defer(uri)`, if any, and navigates to it.
 * Intended to be called after splash-screen work such as login/token validation.
 *
 * @return The result, or null when no deep link is deferred.
 * @throws IllegalStateException If `Wisp.initialize()` has not been called first
 */
fun NavController.navigateToDeferred(): WispResult? {
    return Wisp.getDefaultInstance().navigateToDeferred(this)
}
