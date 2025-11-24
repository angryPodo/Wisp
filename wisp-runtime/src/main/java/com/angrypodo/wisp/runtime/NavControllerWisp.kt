package com.angrypodo.wisp.runtime

import android.net.Uri
import androidx.navigation.NavController

/**
 * URI를 분석하고 즉시 백스택을 새로 구성하여 탐색하는 최종 사용자용 API입니다.
 * 내부적으로 `Wisp.getDefaultInstance()`를 호출하여 모든 작업을 위임합니다.
 *
 * @param uri 딥링크 URI
 * @throws WispError.ParsingFailed URI 파싱에 실패한 경우
 * @throws WispError.UnknownPath `WispRegistry`에 등록되지 않은 경로가 포함된 경우
 * @throws WispError.NavigationFailed 내비게이션 실행에 실패한 경우
 * @throws IllegalStateException `Wisp.initialize()`가 먼저 호출되지 않은 경우
 */
fun NavController.navigateTo(uri: Uri) {
    val wisp = Wisp.getDefaultInstance()
    val routes = wisp.resolveRoutes(uri)
    wisp.navigateTo(this, routes)
}
