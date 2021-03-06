package com.marcosholgado.performancetest.eighthTest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.marcosholgado.performancetest.LeakActivity
import com.marcosholgado.performancetest.R

import org.junit.Test
import org.junit.Rule

class EighthTest {

    @get:Rule
    var mainActivityActivityTestRule = ActivityTestRule(LeakActivity::class.java)

    @Test
    @IgnoreLeaks("Ignore this")
    fun testIgnoreLeaks() {
        onView(withId(R.id.button)).perform(click())
    }

    @Test
    fun testLeaks() {
        onView(withId(R.id.button)).perform(click())
    }
}