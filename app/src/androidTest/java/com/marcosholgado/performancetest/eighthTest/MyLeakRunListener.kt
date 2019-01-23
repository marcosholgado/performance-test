package com.marcosholgado.performancetest.eighthTest

import com.squareup.leakcanary.FailTestOnLeakRunListener
import org.junit.runner.Description

class MyLeakRunListener: FailTestOnLeakRunListener() {
    override fun skipLeakDetectionReason(description: Description): String? {
        return description.getAnnotation(IgnoreLeaks::class.java)?.message
    }
}