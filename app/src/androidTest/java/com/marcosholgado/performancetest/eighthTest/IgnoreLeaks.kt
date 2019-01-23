package com.marcosholgado.performancetest.eighthTest

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class IgnoreLeaks(
    val message: String = ""
)