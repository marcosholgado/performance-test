package com.marcosholgado.performancetest

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.FrameMetrics
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
@LargeTest
class FirstTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)
    var percJankyFrames = 0f

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testFirst() {
        val handler = Handler(Looper.getMainLooper())
        activityRule.activity.window
            .addOnFrameMetricsAvailableListener(object : Window.OnFrameMetricsAvailableListener {

                private var totalFrames = 0
                private var jankyFrames = 0

                override fun onFrameMetricsAvailable(
                    window: Window,
                    frameMetrics: FrameMetrics,
                    dropCount: Int
                ) {
                    totalFrames++
                    val duration =
                        (0.000001 * frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)).toFloat()
                    if (duration > 16f) {
                        jankyFrames++
                        val percentage = jankyFrames.toFloat() / totalFrames * 100
                        percJankyFrames = percentage
                    }
                }
            }, handler)

        for (i in 0..30) {
            Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
        }
        for (i in 30.downTo(1)) {
            Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
        }

        Log.d("First Test", "Percentage of janky frames was $percJankyFrames")

        assertWithMessage("Janky frames over $PERCENTAGE% value was $percJankyFrames%").that(percJankyFrames).isLessThan(PERCENTAGE)
    }

    @Test
    fun testFirstUIAutomator() {
        val handler = Handler(Looper.getMainLooper())
        activityRule.activity.window
            .addOnFrameMetricsAvailableListener(object : Window.OnFrameMetricsAvailableListener {

                private var totalFrames = 0
                private var jankyFrames = 0

                override fun onFrameMetricsAvailable(
                    window: Window,
                    frameMetrics: FrameMetrics,
                    dropCountSinceLastInvocation: Int
                ) {
                    totalFrames++
                    val duration =
                        (0.000001 * frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)).toFloat()
                    if (duration > 16f) {
                        jankyFrames++
                        val percentage = jankyFrames.toFloat() / totalFrames * 100
                        percJankyFrames = percentage
                    }
                }
            }, handler)

        for (i in 0 until 2) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.setAsVerticalList()
            appViews.scrollTextIntoView("This is item 24")
            appViews.scrollTextIntoView("This is item 1")
        }

        Log.d("First Test", "Percentage of janky frames was $percJankyFrames")

        assertWithMessage("Janky frames over $PERCENTAGE_UI% value was $percJankyFrames%").that(percJankyFrames).isLessThan(PERCENTAGE_UI)
    }

    companion object {
        private const val PERCENTAGE = 20f
        private const val PERCENTAGE_UI = 5f
    }
}