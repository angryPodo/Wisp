package com.angrypodo.wisp

import android.app.Application
import com.angrypodo.wisp.generated.WispRegistry
import com.angrypodo.wisp.runtime.Wisp

class WispSampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // KSP가 생성한 WispRegistry를 전달하여 라이브러리를 초기화합니다.
        Wisp.initialize(WispRegistry)
    }
}
