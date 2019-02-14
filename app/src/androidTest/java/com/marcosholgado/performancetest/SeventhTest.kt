package com.marcosholgado.performancetest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule

import org.junit.Test
import org.junit.Rule

class SeventhTest {

    @get:Rule
    var mainActivityActivityTestRule = ActivityTestRule(LeakActivity::class.java)

    @Test
    fun testLeaks() {
        onView(withId(R.id.button)).perform(click())
    }
}