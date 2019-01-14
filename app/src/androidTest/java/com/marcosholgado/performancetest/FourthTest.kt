package com.marcosholgado.performancetest

import android.content.Intent
import android.content.OperationApplicationException
import android.os.RemoteException
import android.os.SystemClock
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.jank.GfxMonitor
import androidx.test.jank.JankTest
import androidx.test.jank.JankTestBase
import androidx.test.jank.annotations.UseMonitorFactory
import androidx.test.uiautomator.UiDevice
import com.marcosholgado.performancetest.helpers.MyFactory

@UseMonitorFactory(MyFactory::class)
class FourthTest : JankTestBase() {

    private lateinit var device: UiDevice

    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        device = UiDevice.getInstance(instrumentation)
        device.setOrientationNatural()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    private fun launchApp(packageName: String) {
        val pm = instrumentation.context.packageManager
        val appIntent = pm.getLaunchIntentForPackage(packageName)
        appIntent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        instrumentation.context.startActivity(appIntent)
        device.waitForIdle()
    }

    @Throws(OperationApplicationException::class, RemoteException::class)
    fun launchApp() {
        launchApp(PACKAGE_NAME)
        device.waitForIdle()
        SystemClock.sleep(200)
    }

    @JankTest(beforeTest = "launchApp", expectedFrames = EXPECTED_PERC_FRAMES, defaultIterationCount = 1)
    @GfxMonitor(processName = PACKAGE_NAME)
    fun testFourth() {
        for (i in 0 until INNER_LOOP) {
            for (i in 0..30) {
                Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                    .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
            }
            for (i in 30.downTo(1)) {
                Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                    .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
            }
        }
        SystemClock.sleep(200)
    }

    companion object {
        private const val INNER_LOOP = 2
        private const val EXPECTED_PERC_FRAMES = 97
        private const val PACKAGE_NAME = "com.marcosholgado.performancetest"
    }
}