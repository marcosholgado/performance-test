package com.marcosholgado.performancetest.sixthtest

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class PerformanceTest(
    val processName: String = "",
    val perfType: PerfType = PerfType.TOTAL_FRAMES,
    val threshold: Int = Int.MAX_VALUE,
    val assertionType: AssertionType = AssertionType.LESS_OR_EQUAL
) {
    enum class PerfType(val type: String) {
        TOTAL_FRAMES("gfx-avg-total-frames"),
        MAX_TOTAL_FRAMES("gfx-max-total-frames"),
        MIN_TOTAL_FRAMES("gfx-min-total-frames"),
        AVG_NUM_JANKY("gfx-avg-jank"),
        MAX_NUM_JANKY("gfx-max-jank"),
        AVG_FRAME_TIME_50TH("gfx-avg-frame-time-50"),
        MAX_FRAME_TIME_50TH("gfx-max-frame-time-50"),
        AVG_FRAME_TIME_90TH("gfx-avg-frame-time-90"),
        MAX_FRAME_TIME_90TH("gfx-max-frame-time-90"),
        AVG_FRAME_TIME_95TH("gfx-avg-frame-time-95"),
        MAX_FRAME_TIME_95TH("gfx-max-frame-time-95"),
        AVG_FRAME_TIME_99TH("gfx-avg-frame-time-99"),
        MAX_FRAME_TIME_99TH("gfx-max-frame-time-99")
    }

    enum class AssertionType(val type: Int) {
        LESS(0),
        LESS_OR_EQUAL(1),
        GREATER(2),
        GREATER_OR_EQUAL(3),
        EQUAL(4)
    }

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