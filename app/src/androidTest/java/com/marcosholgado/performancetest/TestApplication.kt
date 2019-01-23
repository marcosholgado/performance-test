package com.marcosholgado.performancetest

import com.squareup.leakcanary.InstrumentationLeakDetector

open class TestApplication: MyApplication() {
    override fun setupLeakCanary() {
        InstrumentationLeakDetector.instrumentationRefWatcher(this)
            .buildAndInstall()
    }
}