package com.marcosholgado.performancetest

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import kotlinx.android.synthetic.main.activity_leak.*

class KLeakActivity : Activity() {

    private var test: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leak)

        button.setOnClickListener { startAsyncWork() }
    }

    private fun startAsyncWork() {
        val work = Runnable {
            // test = 1 // comment this line to pass the test
            SystemClock.sleep(20000)
        }
        Thread(work).start()
    }
}
