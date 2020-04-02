package com.marcosholgado.performancetest.ninthTest

import leakcanary.FailTestOnLeakRunListener
import org.junit.runner.Description

class MyOtherLeakRunListener: FailTestOnLeakRunListener() {
    override fun skipLeakDetectionReason(description: Description): String? {
        return if(description.getAnnotation(LeakTest::class.java) != null)
            null
        else
            "Skip Leak test"
    }
}