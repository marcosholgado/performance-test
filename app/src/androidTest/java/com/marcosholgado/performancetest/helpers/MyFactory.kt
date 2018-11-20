package com.marcosholgado.performancetest.helpers

import android.app.Instrumentation
import android.os.Build
import android.util.Log
import androidx.test.jank.GfxMonitor
import androidx.test.jank.IMonitor
import androidx.test.jank.internal.JankMonitorFactory
import java.lang.reflect.Method

class MyFactory(instrumentation: Instrumentation?): JankMonitorFactory(instrumentation) {

    override fun getMonitors(testMethod: Method?, testInstance: Any?): MutableList<IMonitor> {
        val monitors = ArrayList<IMonitor>()

        val gfxMonitorArgs = testMethod?.getAnnotation(GfxMonitor::class.java)
        if (gfxMonitorArgs != null) {
            // GfxMonitor only works on M+. NB: Hard coding value since SDK 22 isn't in prebuilts.
            if (API_LEVEL_ACTUAL <= 22) {
                Log.w("TEST", "Skipping GfxMonitor. Not supported by current platform.")
            } else {
                var process = gfxMonitorArgs.processName
                if (process.startsWith("#")) {
                    process = resolveGfxMonitorProcessName(process.substring(1), testInstance)
                }
                monitors.add(MyMonitor(instrumentation, process))
            }
        }

        return monitors
    }

    companion object {
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1

    }
}