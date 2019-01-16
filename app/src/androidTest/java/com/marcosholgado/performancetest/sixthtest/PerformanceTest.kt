package com.marcosholgado.performancetest.sixthtest

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PerformanceTest(val processName: String = "", val percentile: Int = Int.MAX_VALUE)