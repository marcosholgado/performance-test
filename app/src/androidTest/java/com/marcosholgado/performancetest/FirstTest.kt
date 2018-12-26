package com.marcosholgado.performancetest

import android.os.Handler
import android.os.Looper
import android.view.FrameMetrics
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertWithMessage
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
@LargeTest
class FirstTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    private var disposable: Disposable? = null
    var jankFrames = 0f

    @Test
    fun testFirst() {

        disposable = createObservable()
            .subscribeOn(Schedulers.trampoline())
            .observeOn(Schedulers.trampoline())
            .subscribe {
                jankFrames = it
            }

        for (i in 0..30) {
            Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
        }
        for (i in 30.downTo(1)) {
            Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(i))
        }

        assertWithMessage("Janky frames over 20% value was $jankFrames%").that(jankFrames).isLessThan(20f)
    }

    private fun createObservable(): Observable<Float> {
        val handler = Handler(Looper.getMainLooper())
        return Observable.create { emitter ->
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
                        val totalDurationInMillis =
                            (0.000001 * frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)).toFloat()
                        if (totalDurationInMillis > 20f) {
                            jankyFrames++
                            val percentage = jankyFrames.toFloat() / totalFrames * 100
                            emitter.onNext(percentage)
                        }
                    }
                }, handler)
        }
    }
    @After
    fun afterTest() {
        disposable?.dispose()
    }
}