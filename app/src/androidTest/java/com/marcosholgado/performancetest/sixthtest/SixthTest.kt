package com.marcosholgado.performancetest.sixthtest

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.marcosholgado.performancetest.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SixthTest {

    private lateinit var device: UiDevice

    @get:Rule
    var mainActivityActivityTestRule = ActivityPerfTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    @PerformanceTest(
        processName = PACKAGE_NAME,
        perfType = PerformanceTest.PerfType.AVG_FRAME_TIME_95TH,
        threshold = 18,
        assertionType = PerformanceTest.AssertionType.LESS_OR_EQUAL
    )
    fun testSixth() {
        for (i in 0 until INNER_LOOP) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.setAsVerticalList()
            appViews.scrollTextIntoView("This is item 24")
            appViews.scrollTextIntoView("This is item 1")
        }
    }

    @Test
    @PerformanceTest(
        PACKAGE_NAME,
        PerformanceTest.PerfType.AVG_NUM_JANKY,
        5,
        PerformanceTest.AssertionType.LESS_OR_EQUAL
        )
    fun testSixth2() {
        for (i in 0 until INNER_LOOP) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.setAsVerticalList()
            appViews.scrollTextIntoView("This is item 24")
            appViews.scrollTextIntoView("This is item 1")
        }
    }

    companion object {
        private const val INNER_LOOP = 2
        private const val PACKAGE_NAME = "com.marcosholgado.performancetest"
    }
}