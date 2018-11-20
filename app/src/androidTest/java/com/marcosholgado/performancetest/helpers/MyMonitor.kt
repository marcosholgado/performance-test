package com.marcosholgado.performancetest.helpers

import android.app.Instrumentation
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.test.jank.GfxMonitor
import androidx.test.jank.IMonitor

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.EnumMap
import java.util.regex.Pattern

import junit.framework.Assert

/**
 * Monitors dumpsys gfxinfo to detect janky frames.
 *
 * Reports average and max jank. Additionally reports summary statistics for common problems that
 * can lead to dropped frames.
 */
class MyMonitor(
    private val instrumentation: Instrumentation,
    private val process: String
) : IMonitor {

    // Metrics accumulated for each iteration
    private val accumulatedStats = EnumMap<JankStat, List<Number>>(JankStat::class.java)

    // Patterns used for parsing dumpsys gfxinfo output
    enum class JankStat private constructor(
        private val parsePattern: Pattern,
        private val groupIndex: Int,
        internal val type: Class<*>,
        optional: Boolean = false
    ) {
        TOTAL_FRAMES(Pattern.compile("\\s*Total frames rendered: (\\d+)"), 1, Int::class.java),
        NUM_JANKY(
            Pattern.compile("\\s*Janky frames: (\\d+) \\((\\d+(\\.\\d+))%\\)"), 2,
            Double::class.java
        ),
        FRAME_TIME_50TH(Pattern.compile("\\s*50th percentile: (\\d+)ms"), 1, Int::class.java, true),
        FRAME_TIME_90TH(Pattern.compile("\\s*90th percentile: (\\d+)ms"), 1, Int::class.java),
        FRAME_TIME_95TH(Pattern.compile("\\s*95th percentile: (\\d+)ms"), 1, Int::class.java),
        FRAME_TIME_99TH(Pattern.compile("\\s*99th percentile: (\\d+)ms"), 1, Int::class.java),
        NUM_MISSED_VSYNC(Pattern.compile("\\s*Number Missed Vsync: (\\d+)"), 1, Int::class.java),
        NUM_HIGH_INPUT_LATENCY(
            Pattern.compile("\\s*Number High input latency: (\\d+)"), 1,
            Int::class.java
        ),
        NUM_SLOW_UI_THREAD(
            Pattern.compile("\\s*Number Slow UI thread: (\\d+)"),
            1,
            Int::class.java
        ),
        NUM_SLOW_BITMAP_UPLOADS(
            Pattern.compile("\\s*Number Slow bitmap uploads: (\\d+)"), 1,
            Int::class.java
        ),
        NUM_SLOW_DRAW(
            Pattern.compile("\\s*Number Slow issue draw commands: (\\d+)"), 1,
            Int::class.java
        ),
        NUM_FRAME_DEADLINE_MISSED(
            Pattern.compile("\\s*Number Frame deadline missed: (\\d+)"), 1,
            Int::class.java,
            true
        );

        private var successfulParse = false
        internal var isOptional = false

        init {
            isOptional = optional
        }

        internal fun parse(line: String): String? {
            var ret: String? = null
            val matcher = parsePattern.matcher(line)
            if (matcher.matches()) {
                ret = matcher.group(groupIndex)
                successfulParse = true
            }
            return ret
        }

        internal fun wasParsedSuccessfully(): Boolean {
            return successfulParse
        }

        internal fun reset() {
            successfulParse = false
        }
    }


    init {
        for (stat in JankStat.values()) {
            when {
                stat.type == Int::class.java -> accumulatedStats[stat] = ArrayList<Int>()
                stat.type == Double::class.java -> accumulatedStats[stat] = ArrayList<Double>()
                else -> // Shouldn't get here
                    throw IllegalStateException("Unsupported JankStat type")
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun startIteration() {
        // Clear out any previous data
        val stdout = executeShellCommand(
            String.format("dumpsys gfxinfo %s reset", process)
        )
        val reader = BufferedReader(InputStreamReader(stdout))

        reader.use { reader ->
            // Read the output, but don't do anything with it
            while (reader.readLine() != null) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun stopIteration(): Bundle {
        // Dump the latest stats
        val stdout = executeShellCommand(String.format("dumpsys gfxinfo %s", process))
        val reader = BufferedReader(InputStreamReader(stdout))

        // The frame stats section has the following output:
        // Total frames rendered: ###
        // Janky frames: ### (##.##%)
        // 50th percentile: ##ms
        // 90th percentile: ##ms
        // 95th percentile: ##ms
        // 99th percentile: ##ms
        // Number Missed Vsync: #
        // Number High input latency: #
        // Number Slow UI thread: #
        // Number Slow bitmap uploads: #
        // Number Slow draw: #
        // Number Frame deadline missed: #

        reader.use { reader ->
            var line: String? = reader.readLine()
            do {
                // Attempt to parse the line as a frame stat value
                for (stat in JankStat.values()) {
                    val part: String? = stat.parse(line!!)
                    if (part != null) {
                        // Parse was successful. Add the numeric value to the accumulated list of
                        // values for that stat.
                        if (stat.type == Int::class.java) {
                            val stats = accumulatedStats[stat] as MutableList<Int>
                            stats.add(Integer.valueOf(part!!))
                        } else if (stat.type == Double::class.java) {
                            val stats = accumulatedStats[stat] as MutableList<Double>
                            stats.add(java.lang.Double.valueOf(part!!))
                        } else {
                            // Shouldn't get here
                            throw IllegalStateException("Unsupported JankStat type")
                        }
                        break
                    }
                }
                line = reader.readLine()
            } while (line != null)
        }

        // Make sure we found all the stats
        for (stat in JankStat.values()) {
            if (!stat.wasParsedSuccessfully() && !stat.isOptional) {
                Assert.fail(String.format("Failed to parse %s", stat.name))
            }
            stat.reset()
        }

        val jankyFreeFrames = accumulatedStats[JankStat.NUM_JANKY] as List<Int>

        // TODO(allenhair): Return full itermediate results.
        val ret = Bundle()
        ret.putInt("num-frames", 100 - jankyFreeFrames[jankyFreeFrames
            .size - 1])
        return ret
    }

    private fun putAvgMaxInteger(
        metrics: Bundle, averageKey: String, maxKey: String,
        values: List<Int>
    ) {

        metrics.putDouble(averageKey, computeAverageInt(values))
        metrics.putInt(maxKey, Collections.max(values))
    }

    private fun putAvgMaxDouble(
        metrics: Bundle, averageKey: String, maxKey: String,
        values: List<Double>
    ) {

        metrics.putDouble(averageKey, computeAverageDouble(values))
        metrics.putDouble(maxKey, Collections.max(values))
    }

    private fun transformToPercentage(values: List<Int>, totals: List<Int>): List<Double> {
        val ret = ArrayList<Double>(values.size)

        val valuesItr = values.iterator()
        val totalsItr = totals.iterator()
        while (valuesItr.hasNext()) {
            val value = valuesItr.next().toDouble()
            val total = totalsItr.next().toDouble()

            ret.add(value / total * 100.0f)
        }

        return ret
    }

    private fun computeAverage(values: List<Int>): Int {
        var sum = 0

        for (value in values) {
            sum += value
        }

        return sum / values.size
    }

    /**
     * {@inheritDoc}
     */
    override fun getMetrics(): Bundle {
        val metrics = Bundle()

        // Retrieve the total number of frames
        val totals = accumulatedStats[JankStat.TOTAL_FRAMES] as List<Int>

        // Store avg, min and max of total frames
        metrics.putInt(GfxMonitor.KEY_AVG_TOTAL_FRAMES, computeAverage(totals))
        metrics.putInt(GfxMonitor.KEY_MAX_TOTAL_FRAMES, Collections.max(totals))
        metrics.putInt(GfxMonitor.KEY_MIN_TOTAL_FRAMES, Collections.min(totals))

        // Store average and max jank
        putAvgMaxDouble(
            metrics, GfxMonitor.KEY_AVG_NUM_JANKY, GfxMonitor.KEY_MAX_NUM_JANKY,
            accumulatedStats[JankStat.NUM_JANKY] as List<Double>
        )

        // Store average and max percentile frame times
        val statsFor50TH = accumulatedStats[JankStat.FRAME_TIME_50TH] as List<Int>
        if (!statsFor50TH.isEmpty()) {
            // 50th percentile frame is optional, because it wasn't available in the initial version
            // of the gfxinfo service
            putAvgMaxInteger(
                metrics, GfxMonitor.KEY_AVG_FRAME_TIME_50TH_PERCENTILE,
                GfxMonitor.KEY_MAX_FRAME_TIME_50TH_PERCENTILE, statsFor50TH
            )
        }
        putAvgMaxInteger(
            metrics, GfxMonitor.KEY_AVG_FRAME_TIME_90TH_PERCENTILE,
            GfxMonitor.KEY_MAX_FRAME_TIME_90TH_PERCENTILE,
            accumulatedStats[JankStat.FRAME_TIME_90TH] as List<Int>
        )
        putAvgMaxInteger(
            metrics, GfxMonitor.KEY_AVG_FRAME_TIME_95TH_PERCENTILE,
            GfxMonitor.KEY_MAX_FRAME_TIME_95TH_PERCENTILE,
            accumulatedStats[JankStat.FRAME_TIME_95TH] as List<Int>
        )
        putAvgMaxInteger(
            metrics, GfxMonitor.KEY_AVG_FRAME_TIME_99TH_PERCENTILE,
            GfxMonitor.KEY_MAX_FRAME_TIME_99TH_PERCENTILE,
            accumulatedStats[JankStat.FRAME_TIME_99TH] as List<Int>
        )

        // Store average and max missed vsync
        val missedVsyncPercent = transformToPercentage(
            accumulatedStats[JankStat.NUM_MISSED_VSYNC] as List<Int>, totals
        )
        putAvgMaxDouble(
            metrics, GfxMonitor.KEY_AVG_MISSED_VSYNC, GfxMonitor.KEY_MAX_MISSED_VSYNC,
            missedVsyncPercent
        )

        // Store average and max high input latency
        val highInputLatencyPercent = transformToPercentage(
            accumulatedStats[JankStat.NUM_HIGH_INPUT_LATENCY] as List<Int>, totals
        )
        putAvgMaxDouble(
            metrics, GfxMonitor.KEY_AVG_HIGH_INPUT_LATENCY,
            GfxMonitor.KEY_MAX_HIGH_INPUT_LATENCY, highInputLatencyPercent
        )

        // Store average and max slow ui thread
        val slowUiThreadPercent = transformToPercentage(
            accumulatedStats[JankStat.NUM_SLOW_UI_THREAD] as List<Int>, totals
        )
        putAvgMaxDouble(
            metrics, GfxMonitor.KEY_AVG_SLOW_UI_THREAD,
            GfxMonitor.KEY_MAX_SLOW_UI_THREAD, slowUiThreadPercent
        )

        // Store average and max slow bitmap uploads
        val slowBitMapUploadsPercent = transformToPercentage(
            accumulatedStats[JankStat.NUM_SLOW_BITMAP_UPLOADS] as List<Int>, totals
        )
        putAvgMaxDouble(
            metrics, GfxMonitor.KEY_AVG_SLOW_BITMAP_UPLOADS,
            GfxMonitor.KEY_MAX_SLOW_BITMAP_UPLOADS, slowBitMapUploadsPercent
        )

        // Store average and max slow draw
        val slowDrawPercent = transformToPercentage(
            accumulatedStats[JankStat.NUM_SLOW_DRAW] as List<Int>, totals
        )
        putAvgMaxDouble(
            metrics, GfxMonitor.KEY_AVG_SLOW_DRAW, GfxMonitor.KEY_MAX_SLOW_DRAW,
            slowDrawPercent
        )

        // Store average and max of number of frame deadline missed
//        putAvgMaxInteger(
//            metrics, GfxMonitor.KEY_AVG_NUM_FRAME_MISSED,
//            GfxMonitor.KEY_MAX_NUM_FRAME_MISSED,
//            accumulatedStats[JankStat.NUM_FRAME_DEADLINE_MISSED] as List<Int>
//        )

        return metrics
    }

    private fun getMatchGroup(input: String, pattern: Pattern, groupIndex: Int): String? {
        var ret: String? = null
        val matcher = pattern.matcher(input)
        if (matcher.matches()) {
            ret = matcher.group(groupIndex)
        }
        return ret
    }

    /**
     * Executes the given `command` as the shell user and returns an [InputStream]
     * containing the command's standard output.
     */
    private fun executeShellCommand(command: String): InputStream {
        val stdout = instrumentation.uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(stdout)
    }

    private fun computeAverageDouble(values: Collection<Double>): Double {
        var sum = 0.0
        for (value in values) {
            sum += value
        }
        return sum / values.size
    }

    /** Compute the average value from a collection of integers.  */
    private fun computeAverageInt(values: Collection<Int>): Double {
        var sum = 0.0
        for (value in values) {
            sum += value.toDouble()
        }
        return sum / values.size
    }
}