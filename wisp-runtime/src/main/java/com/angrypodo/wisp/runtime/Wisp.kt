package com.angrypodo.wisp.runtime

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavDestination
import androidx.navigation.NavType

/**
 * Wisp 라이브러리의 핵심 로직을 수행하고, 내비게이션 기능을 실행하는 클래스입니다.
 * [WispRegistrySpec]과 [WispUriParser]를 주입받아 URI를 라우트 객체 리스트로 변환하고,
 * [NavController]를 통해 실제 탐색을 수행합니다.
 *
 * 고급 사용자는 이 클래스를 직접 생성하여 DI 컨테이너로 관리할 수 있습니다.
 * 대부분의 사용자는 [Wisp.initialize]와 [NavController.navigateTo] 확장 함수를 통해 Wisp를 간접적으로 사용합니다.
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
     * 백스택 생성을 위해 NavDeepLinkBuilder를 사용합니다.
     */
    fun navigateTo(navController: NavController, context: Context, routes: List<Any>) {
        if (routes.isEmpty()) return

        try {
            val builder = NavDeepLinkBuilder(context).setGraph(navController.graph)
            routes.forEach { route ->
                val routePattern = registry.getRoutePattern(route)
                    ?: throw IllegalArgumentException(
                        "Route pattern not found for ${route::class.simpleName}"
                    )

                val destination = navController.graph.findNode(routePattern)
                    ?: throw IllegalArgumentException(
                        "Destination not found for route pattern: $routePattern"
                    )

                builder.addDestination(destination.id, destination.buildArguments(route))
            }
            builder.createPendingIntent().send()
        } catch (e: Exception) {
            throw WispError.NavigationFailed(
                reason = e::class.simpleName ?: "Unknown",
                detail = e.message
            )
        }
    }

    /**
     * Wisp의 기본 인스턴스를 제공하고 초기화하는 역할을 담당합니다.
     */
    companion object {
        private var instance: Wisp? = null

        /**
         * 대부분의 사용자를 위한 초기화 함수입니다.
         * Application.onCreate()에서 KSP가 생성한 WispRegistry를 전달하여 호출합니다.
         */
        fun initialize(registry: WispRegistrySpec) {
            if (instance == null) {
                instance = Wisp(registry)
            }
        }

        /**
         * 라이브러리 내부에서 기본 인스턴스를 사용하기 위한 함수입니다.
         * @throws IllegalStateException Wisp.initialize()가 먼저 호출되지 않은 경우
         */
        internal fun getDefaultInstance(): Wisp {
            return instance ?: throw IllegalStateException(
                "Wisp.initialize() must be called first in your Application class."
            )
        }
    }
}

/**
 * NavDestination의 정보를 기반으로 route 객체로부터 Bundle을 생성합니다.
 * 타입 이름을 비교하여, 여러 인자 중 @Serializable 객체 자신을 담는 인자를 정확히 찾아냅니다.
 */
@Suppress("UNCHECKED_CAST")
private fun NavDestination.buildArguments(route: Any): Bundle? {
    val argumentEntry = arguments.entries.find { (_, arg) ->
        arg.type.name == route::class.qualifiedName
    }

    if (argumentEntry == null) {
        return if (arguments.isEmpty()) null else Bundle()
    }

    val argumentName = argumentEntry.key
    val navType = argumentEntry.value.type as NavType<Any>

    return Bundle().apply {
        navType.put(this, argumentName, route)
    }
}
