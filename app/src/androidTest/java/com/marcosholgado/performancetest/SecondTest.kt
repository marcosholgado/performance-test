package com.marcosholgado.performancetest

import android.content.Intent
import android.content.OperationApplicationException
import android.os.RemoteException
import android.os.SystemClock
import androidx.test.jank.GfxMonitor
import androidx.test.jank.JankTest
import androidx.test.jank.JankTestBase
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import org.junit.Rule

class SecondTest : JankTestBase() {

    private lateinit var device: UiDevice

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        device = UiDevice.getInstance(instrumentation)
        device.setOrientationNatural()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        device.unfreezeRotation()
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

    @JankTest(beforeTest = "launchApp", expectedFrames = EXPECTED_FRAMES, defaultIterationCount = 1)
    @GfxMonitor(processName = PACKAGE_NAME)
    fun testSecond() {
        for (i in 0 until INNER_LOOP) {
            val appViews = UiScrollable(UiSelector().scrollable(true))
            appViews.setAsVerticalList()
            appViews.scrollTextIntoView("This is item 24")
            appViews.scrollTextIntoView("This is item 1")
        }
        SystemClock.sleep(200)
    }

    companion object {
        private const val INNER_LOOP = 2
        private const val EXPECTED_FRAMES = 800
        private const val PACKAGE_NAME = "com.marcosholgado.performancetest"
    }
}