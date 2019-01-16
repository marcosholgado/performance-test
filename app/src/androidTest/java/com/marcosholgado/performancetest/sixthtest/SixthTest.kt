package com.marcosholgado.performancetest.sixthtest

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.marcosholgado.performancetest.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SixthTest {

    private lateinit var device: UiDevice

    @get:Rule
    var mainActivityActivityTestRule = ActivityPerfTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    @PerformanceTest(PACKAGE_NAME, EXPECTED_MSECS)
    fun testSixth() {
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