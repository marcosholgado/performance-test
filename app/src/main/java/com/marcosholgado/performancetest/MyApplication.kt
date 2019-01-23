package com.marcosholgado.performancetest

import android.app.Application
import android.os.StrictMode
import com.squareup.leakcanary.LeakCanary

open class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupLeakCanary()
    }

    open fun setupLeakCanary() {
        enabledStrictMode()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }
        LeakCanary.install(this)
    }

    private fun enabledStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build()
        )
    }
}
