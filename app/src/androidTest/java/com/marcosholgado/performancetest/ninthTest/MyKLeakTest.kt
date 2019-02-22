package com.marcosholgado.performancetest.ninthTest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.marcosholgado.performancetest.KLeakActivity
import com.marcosholgado.performancetest.R

import org.junit.Test
import org.junit.Rule

class MyKLeakTest {

    @get:Rule
    var mainActivityActivityTestRule = ActivityTestRule(KLeakActivity::class.java)

    @Test
    @LeakTest
    fun testLeaks() {
        onView(withId(R.id.button)).perform(click())
    }
}