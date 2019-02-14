package com.marcosholgado.performancetest.helpers

import android.app.Instrumentation
import android.os.Build
import android.util.Log
import androidx.test.jank.GfxMonitor
import androidx.test.jank.IMonitor
import androidx.test.jank.IMonitorFactory
import java.lang.reflect.Method

class MyFactory(private val instrumentation: Instrumentation): IMonitorFactory {

    override fun getMonitors(testMethod: Method?, testInstance: Any?): MutableList<IMonitor> {
        val monitors = mutableListOf<IMonitor>()

        val gfxMonitorArgs = testMethod?.getAnnotation(GfxMonitor::class.java)
        if (gfxMonitorArgs != null) {
            // GfxMonitor only works on M+. NB: Hard coding value since SDK 22 isn't in prebuilts.
            if (API_LEVEL_ACTUAL <= 22) {
                Log.w("PerfTest", "Skipping GfxMonitor. Not supported by current platform.")
            } else {
                monitors.add(MyMonitor(instrumentation, gfxMonitorArgs.processName))
            }
        }
        return monitors
    }

    companion object {
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1
    }
}