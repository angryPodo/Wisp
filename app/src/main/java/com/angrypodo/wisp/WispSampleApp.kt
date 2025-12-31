package com.angrypodo.wisp

import android.app.Application
import com.angrypodo.wisp.runtime.Wisp

class WispSampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Wisp.initialize()
    }
}
