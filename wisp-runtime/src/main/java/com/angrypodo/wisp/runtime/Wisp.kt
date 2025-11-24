package com.angrypodo.wisp.runtime

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Wisp 라이브러리의 핵심 로직을 수행하고, 내비게이션 기능을 실행하는 클래스입니다.
 */
class Wisp(
    private val registry: WispRegistrySpec,
    private val parser: WispUriParser = DefaultWispUriParser()
) {

    /**
     * URI를 분석하여 @Serializable 라우트 객체의 리스트로 변환합니다.
     * @throws WispError.UnknownPath 등록되지 않은 경로가 포함된 경우
     */
    fun resolveRoutes(uri: Uri): List<Any> {
        val paths = parser.parse(uri)
        return paths.map { path ->
            registry.createRoute(path) ?: throw WispError.UnknownPath(path)
        }
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

        fun initialize(registry: WispRegistrySpec) {
            if (instance == null) {
                instance = Wisp(registry)
            }
        }

        internal fun getDefaultInstance(): Wisp {
            return instance ?: throw IllegalStateException(
                "Wisp.initialize() must be called first in your Application class."
            )
        }
    }
}
