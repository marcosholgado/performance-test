package com.marcosholgado.performancetest.ninthTest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.marcosholgado.performancetest.LeakActivity
import com.marcosholgado.performancetest.R
import org.junit.Before

import org.junit.Test
import org.junit.Rule

class NinthTest {

    private lateinit var device: UiDevice

    @get:Rule
    var mainActivityActivityTestRule = ActivityTestRule(LeakActivity::class.java)

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.unfreezeRotation()
    }

    @Test
    fun testIgnoreLeaks() {
        onView(withId(R.id.button)).perform(click())
    }

    @Test
    @LeakTest
    fun testLeaks() {
        onView(withId(R.id.button)).perform(click())
    }
}