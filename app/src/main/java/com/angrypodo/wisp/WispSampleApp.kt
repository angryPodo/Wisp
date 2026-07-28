package com.angrypodo.wisp

import android.app.Application
import android.util.Log
import com.angrypodo.wisp.runtime.Wisp

class WispSampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Wisp.initialize(
            onError = { error -> Log.e("WispSample", "Deep link failed", error) }
        )
    }
}
