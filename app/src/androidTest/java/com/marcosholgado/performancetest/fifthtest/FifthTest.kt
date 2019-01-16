package com.marcosholgado.performancetest.fifthtest

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.jank.IMonitor
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.marcosholgado.performancetest.MainActivity
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FifthTest {

    private lateinit var device: UiDevice
    private var monitor: IMonitor

    init  {
        if (API_LEVEL_ACTUAL <= 22) {
            error("Not supported by current platform.")
        } else {
            monitor = PercentileMonitor(InstrumentationRegistry.getInstrumentation(), PACKAGE_NAME)
        }
    }

    @get:Rule
    var mainActivityActivityTestRule: ActivityTestRule<MainActivity> =
        object : ActivityTestRule<MainActivity>(MainActivity::class.java) {

            override fun beforeActivityLaunched() {
                monitor.startIteration()
                super.beforeActivityLaunched()
            }

            override fun afterActivityFinished() {
                val results = monitor.stopIteration()
                val percentile = results.getInt("percentilesValue", Integer.MAX_VALUE)

                TestCase.assertTrue(
                    String.format(
                        "Too few frames received. Monitor: %s, Expected: %d, Received: %d.",
                        monitor::class.java.simpleName, EXPECTED_MSECS,
                        percentile
                    ),
                    percentile < EXPECTED_MSECS
                )
                super.afterActivityFinished()
            }
        }

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }


    @Test
    fun testFifth() {
        for (i in 0 until INNER_LOOP) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.setAsVerticalList()
            appViews.scrollTextIntoView("This is item 24")
            appViews.scrollTextIntoView("This is item 1")
        }
    }


    companion object {
        private const val INNER_LOOP = 2
        private const val EXPECTED_MSECS = 18
        private const val PACKAGE_NAME = "com.marcosholgado.performancetest"
        internal val API_LEVEL_ACTUAL =
            Build.VERSION.SDK_INT + if ("REL" == Build.VERSION.CODENAME) 0 else 1
    }
}