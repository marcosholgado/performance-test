package com.marcosholgado.performancetest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import org.junit.Before

import org.junit.Test
import org.junit.Rule

class SeventhTest {

    private lateinit var device: UiDevice

    @get:Rule
    var mainActivityActivityTestRule = ActivityTestRule(LeakActivity::class.java)

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.unfreezeRotation()
    }

    @Test
    fun testLeaks() {
        onView(withId(R.id.button)).perform(click())
    }
}