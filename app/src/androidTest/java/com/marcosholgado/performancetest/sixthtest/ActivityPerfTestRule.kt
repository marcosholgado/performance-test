package com.marcosholgado.performancetest.sixthtest

import android.app.Activity
import android.os.Build
import androidx.test.jank.IMonitor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.marcosholgado.performancetest.fifthtest.PercentileMonitor
import junit.framework.TestCase
import org.junit.runner.Description
import org.junit.runners.model.Statement


open class ActivityPerfTestRule<T: Activity>(activityClass: Class<T>): ActivityTestRule<T>(activityClass) {

    private var monitor: IMonitor? = null
    private var annotation: PerformanceTest? = null

    init {
        if (API_LEVEL_ACTUAL <= 22) {
            error("Not supported by current platform.")
        }
    }

    override fun apply(base: Statement?, description: Description?): Statement {
        annotation = description?.getAnnotation(PerformanceTest::class.java)
        annotation?.let {
            monitor = PercentileMonitor(InstrumentationRegistry.getInstrumentation(), it.processName)
        }
        return super.apply(base, description)
    }

    override fun beforeActivityLaunched() {
        monitor?.startIteration()
        super.beforeActivityLaunched()
    }

    override fun afterActivityFinished() {
        monitor?.let {
            val results = it.stopIteration()
            val percentile = results?.getInt("percentilesValue", Integer.MAX_VALUE)

            TestCase.assertTrue(
                String.format(
                    "Too few frames received. Monitor: %s, Expected: %d, Received: %d.",
                    it::class.java.simpleName, annotation!!.percentile,
                    percentile
                ),
                percentile!! < annotation!!.percentile
            )
        }
        super.afterActivityFinished()
    }

    companion object {
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1
    }
}

