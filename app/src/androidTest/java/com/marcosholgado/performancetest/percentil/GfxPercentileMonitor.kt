package com.marcosholgado.performancetest.percentil

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class GfxPercentileMonitor {
    companion object {
        const val KEY_AVG_TOTAL_FRAMES = "gfx-avg-total-frames"
        const val KEY_MAX_TOTAL_FRAMES = "gfx-max-total-frames"
        const val KEY_MIN_TOTAL_FRAMES = "gfx-min-total-frames"
        const val KEY_AVG_NUM_JANKY = "gfx-avg-jank"
        const val KEY_MAX_NUM_JANKY = "gfx-max-jank"
        const val KEY_AVG_FRAME_TIME_50TH_PERCENTILE = "gfx-avg-frame-time-50"
        const val KEY_MAX_FRAME_TIME_50TH_PERCENTILE = "gfx-max-frame-time-50"
        const val KEY_AVG_FRAME_TIME_90TH_PERCENTILE = "gfx-avg-frame-time-90"
        const val KEY_MAX_FRAME_TIME_90TH_PERCENTILE = "gfx-max-frame-time-90"
        const val KEY_AVG_FRAME_TIME_95TH_PERCENTILE = "gfx-avg-frame-time-95"
        const val KEY_MAX_FRAME_TIME_95TH_PERCENTILE = "gfx-max-frame-time-95"
        const val KEY_AVG_FRAME_TIME_99TH_PERCENTILE = "gfx-avg-frame-time-99"
        const val KEY_MAX_FRAME_TIME_99TH_PERCENTILE = "gfx-max-frame-time-99"
    }
}
