package com.marcosholgado.performancetest.sixthtest

import android.os.Build
import android.util.Log
import androidx.test.jank.IMonitor
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase
import org.junit.runner.Description
import org.junit.runner.notification.RunListener

class PerfRunListener: RunListener() {

    private lateinit var monitor: IMonitor
    private lateinit var annotation: PerformanceTest

    override fun testStarted(description: Description?) {
        init(description!!)
        monitor.startIteration()
        super.testStarted(description)
    }

    override fun testFinished(description: Description?) {
        val results = monitor.stopIteration()
        val percentile = results.getInt("percentilesValue", Integer.MAX_VALUE)

        TestCase.assertTrue(
            String.format(
                "Too few frames received. Monitor: %s, Expected: %d, Received: %d.",
                monitor::class.java.simpleName, 17,
                percentile
            ),
            percentile < 17
        )
        super.testFinished(description)
    }

    private fun init(description: Description) {
        if (API_LEVEL_ACTUAL <= 22) {
            error("Not supported by current platform.")
        } else {
            annotation = description.getAnnotation(PerformanceTest::class.java)
            Log.d("MARCOS", "Annotacion value is ${annotation.threshold}")
            monitor = PerfMonitor(InstrumentationRegistry.getInstrumentation(), annotation.processName)
        }
    }

    companion object {
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1
    }

}