package com.marcosholgado.performancetest.sixthtest

import android.app.Activity
import android.os.Build
import androidx.test.jank.IMonitor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
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
            monitor = PerfMonitor(InstrumentationRegistry.getInstrumentation(), it.processName)
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
            val type = annotation?.perfType?.type
            val res: Double = results.getDouble(type, results.getInt(type).toDouble())

            val assertion = when(annotation?.assertionType) {
                PerformanceTest.AssertionType.LESS -> res < annotation!!.threshold
                PerformanceTest.AssertionType.LESS_OR_EQUAL -> res <= annotation!!.threshold
                PerformanceTest.AssertionType.GREATER -> res > annotation!!.threshold
                PerformanceTest.AssertionType.GREATER_OR_EQUAL -> res >= annotation!!.threshold
                PerformanceTest.AssertionType.EQUAL -> res == annotation!!.threshold.toDouble()
                null -> false
            }
            TestCase.assertTrue(
                String.format(
                    "Monitor: %s, Expected: %d, Received: %f.",
                    type, annotation!!.threshold,
                    res
                ),
                assertion
            )
        }
        super.afterActivityFinished()
    }

    companion object {
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1
    }
}

