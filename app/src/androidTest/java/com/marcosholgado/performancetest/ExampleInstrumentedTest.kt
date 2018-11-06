package com.marcosholgado.performancetest

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Rule

@RunWith(AndroidJUnit4::class)
@LargeTest
class MyClassTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test fun scrollsThroughList() {
        val appContext: Context = ApplicationProvider.getApplicationContext()

        for (i in 0..999) {
            onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
        }
    }
}