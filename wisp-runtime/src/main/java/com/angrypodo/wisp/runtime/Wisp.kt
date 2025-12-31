package com.angrypodo.wisp.runtime

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.angrypodo.wisp.runtime.matcher.WispUriMatcher
import com.angrypodo.wisp.runtime.parser.DefaultWispUriParser
import com.angrypodo.wisp.runtime.parser.WispUriParser
import com.angrypodo.wisp.runtime.spi.RouteFactory
import com.angrypodo.wisp.runtime.spi.WispModuleRegistry
import java.util.ServiceLoader

/**
 * Wisp 라이브러리의 핵심 로직을 수행하고, 내비게이션 기능을 실행하는 클래스입니다.
 */
class Wisp(
    private val mergedRoutes: Map<String, RouteFactory>,
    private val parser: WispUriParser = DefaultWispUriParser()
) {

    /**
     * URI를 분석하여 @Serializable 라우트 객체의 리스트로 변환합니다.
     * @throws WispError.UnknownPath 등록되지 않은 경로가 포함된 경우
     */
    fun resolveRoutes(uri: Uri): List<Any> {
        val paths = parser.parse(uri)
        return paths.map { path ->
            matchAndCreate(path) ?: throw WispError.UnknownPath(path)
        }
    }

    private fun matchAndCreate(path: String): Any? {
        for ((pattern, factory) in mergedRoutes) {
            val params = WispUriMatcher.match(path, pattern)
            if (params != null) {
                return factory.create(params)
            }
        }
        return null
    }

    /**
     * 주어진 라우트 객체 리스트를 사용하여 백스택을 새로 구성하고 탐색합니다.
     * NavController.navigate를 순차적으로 호출하여 백스택을 구성합니다.
     */
    fun navigateTo(navController: NavController, routes: List<Any>) {
        if (routes.isEmpty()) return

        try {
            val firstRoute = routes.first()
            navController.navigate(firstRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }

            routes.drop(1).forEach { route ->
                navController.navigate(route)
            }
        } catch (e: Exception) {
            throw WispError.NavigationFailed(
                reason = e::class.simpleName ?: "Unknown",
                detail = e.message
            )
        }
    }

    companion object {
        private var instance: Wisp? = null

        @JvmStatic
        @Synchronized
        fun initialize() {
            if (instance == null) {
                val aggregatedRoutes = mutableMapOf<String, RouteFactory>()
                val loader = ServiceLoader.load(WispModuleRegistry::class.java)

                for (registry in loader) {
                    aggregatedRoutes.putAll(registry.getRoutes())
                }

                instance = Wisp(aggregatedRoutes)
            }
        }

        fun getDefaultInstance(): Wisp {
            return instance ?: throw IllegalStateException(
                "Wisp.initialize() must be called first in your Application class."
            )
        }
    }
}
