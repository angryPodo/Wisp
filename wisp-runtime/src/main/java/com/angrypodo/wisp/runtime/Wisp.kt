package com.angrypodo.wisp.runtime

import android.net.Uri
import androidx.navigation.NavController
import com.angrypodo.wisp.runtime.matcher.WispRouteResolver
import com.angrypodo.wisp.runtime.parser.DefaultWispUriParser
import com.angrypodo.wisp.runtime.parser.WispUriParser
import com.angrypodo.wisp.runtime.spi.RouteFactory
import com.angrypodo.wisp.runtime.spi.WispModuleRegistry
import java.util.ServiceLoader
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException

/**
 * Core entry point of the Wisp library. Resolves deep link URIs into route
 * objects and performs the navigation.
 *
 * @param routes Map of route pattern string -> [RouteFactory]
 * @param parser Splits a URI path into path segments
 * @param onError Global callback invoked whenever deep link handling fails
 *                (logging, fallback policies, etc.)
 */
class Wisp(
    routes: Map<String, RouteFactory>,
    private val parser: WispUriParser = DefaultWispUriParser(),
    private val onError: ((WispError) -> Unit)? = null
) {
    private val resolver = WispRouteResolver(routes)
    private var deferredUri: Uri? = null

    /**
     * Resolves the URI into a list of route objects in backstack order.
     * Never throws; failures are returned as [WispResult.Failure].
     */
    fun resolve(uri: Uri): WispResult {
        val pathSegments = parser.parse(uri)
        if (pathSegments.isEmpty()) {
            return failure(WispError.ParsingFailed(uri.toString(), "URI has no path segments."))
        }

        return when (val matched = resolver.resolve(pathSegments)) {
            is WispRouteResolver.Result.Unmatched ->
                failure(WispError.UnknownPath(matched.segment))

            is WispRouteResolver.Result.Matched ->
                createRoutes(matched.matches, getQueryParams(uri))
        }
    }

    /**
     * Resolves the URI and rebuilds the backstack. The first route clears the
     * existing backstack, and the remaining routes are navigated sequentially
     * on top of it.
     */
    fun navigateTo(navController: NavController, uri: Uri): WispResult {
        val resolved = resolve(uri)
        if (resolved !is WispResult.Success) return resolved

        return try {
            performNavigation(navController, resolved.routes)
            resolved
        } catch (e: Exception) {
            failure(WispError.NavigationFailed(e))
        }
    }

    /**
     * Holds a deep link URI instead of executing it immediately.
     * Use this to gate deep links behind splash-screen work such as
     * login/token validation, then execute via [navigateToDeferred].
     */
    fun defer(uri: Uri?) {
        deferredUri = uri
    }

    /**
     * Consumes the deferred URI, if any, and navigates to it.
     * Returns null when no URI is deferred.
     */
    fun navigateToDeferred(navController: NavController): WispResult? {
        val uri = consumeDeferredUri() ?: return null
        return navigateTo(navController, uri)
    }

    internal fun consumeDeferredUri(): Uri? {
        val uri = deferredUri
        deferredUri = null
        return uri
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createRoutes(
        matches: List<WispRouteResolver.Match>,
        queryParams: Map<String, String>
    ): WispResult {
        val routes = matches.map { match ->
            // Path variables take precedence over query parameters.
            val params = queryParams + match.pathVariables
            try {
                match.factory.create(params)
            } catch (e: MissingFieldException) {
                return failure(
                    WispError.MissingParameter(match.pattern, e.missingFields.joinToString())
                )
            } catch (e: Exception) {
                return failure(WispError.InvalidParameter(match.pattern, e.message))
            }
        }
        return WispResult.Success(routes)
    }

    private fun performNavigation(navController: NavController, routes: List<Any>) {
        navController.navigate(routes.first()) {
            // Pop against the graph id so the whole backstack is cleared even
            // when the start destination has already left the stack
            // (e.g. a warm-start deep link).
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }

        routes.drop(1).forEach { route ->
            navController.navigate(route)
        }
    }

    private fun getQueryParams(uri: Uri): Map<String, String> {
        val params = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { key ->
            uri.getQueryParameter(key)?.let { value ->
                params[key] = value
            }
        }
        return params
    }

    private fun failure(error: WispError): WispResult.Failure {
        onError?.invoke(error)
        return WispResult.Failure(error)
    }

    companion object {
        private var instance: Wisp? = null

        /**
         * Initializes the default instance by collecting route registries from
         * every module via ServiceLoader. Call this in Application.onCreate.
         */
        @JvmStatic
        @Synchronized
        fun initialize(
            parser: WispUriParser = DefaultWispUriParser(),
            onError: ((WispError) -> Unit)? = null
        ) {
            if (instance != null) return

            val aggregatedRoutes = mutableMapOf<String, RouteFactory>()
            val loader = ServiceLoader.load(WispModuleRegistry::class.java)
            for (registry in loader) {
                aggregatedRoutes.putAll(registry.getRoutes())
            }

            instance = Wisp(aggregatedRoutes, parser, onError)
        }

        fun getDefaultInstance(): Wisp {
            return instance ?: throw IllegalStateException(
                "Wisp.initialize() must be called first in your Application class."
            )
        }

        /**
         * Defers a deep link received in an Activity's onCreate/onNewIntent.
         */
        fun defer(uri: Uri?) {
            getDefaultInstance().defer(uri)
        }
    }
}
