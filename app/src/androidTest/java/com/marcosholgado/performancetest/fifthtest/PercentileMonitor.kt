package com.marcosholgado.performancetest.fifthtest

import android.app.Instrumentation
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
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
class PercentileMonitor(
    private val instrumentation: Instrumentation,
    private val process: String
) : IMonitor {

    // Metrics accumulated for each iteration
    private val accumulatedStats = EnumMap<JankStat, List<Number>>(JankStat::class.java)

    // Patterns used for parsing dumpsys gfxinfo output
    enum class JankStat constructor(
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
        FRAME_TIME_99TH(Pattern.compile("\\s*99th percentile: (\\d+)ms"), 1, Int::class.java);

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

        reader.use { reader ->
            var line: String? = reader.readLine()
            do {
                // Attempt to parse the line as a frame stat value
                for (stat in JankStat.values()) {
                    val part: String? = stat.parse(line!!)
                    if (part != null) {
                        // Parse was successful. Add the numeric value to the accumulated list of
                        // values for that stat.
                        when {
                            stat.type == Int::class.java -> {
                                val stats = accumulatedStats[stat] as MutableList<Int>
                                stats.add(Integer.valueOf(part))
                            }
                            stat.type == Double::class.java -> {
                                val stats = accumulatedStats[stat] as MutableList<Double>
                                stats.add(java.lang.Double.valueOf(part!!))
                            }
                            else -> // Shouldn't get here
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

        val percentileTimeInMs = accumulatedStats[JankStat.FRAME_TIME_99TH] as List<Int>
        val ret = Bundle()

        Log.d("MARCOS", "Time is ${accumulatedStats[JankStat.FRAME_TIME_50TH]!![accumulatedStats[JankStat.FRAME_TIME_50TH]!!.size - 1]} FRAME_TIME_50TH")
        Log.d("MARCOS", "Time is ${accumulatedStats[JankStat.FRAME_TIME_90TH]!![accumulatedStats[JankStat.FRAME_TIME_90TH]!!.size - 1]} FRAME_TIME_90TH")
        Log.d("MARCOS", "Time is ${accumulatedStats[JankStat.FRAME_TIME_95TH]!![accumulatedStats[JankStat.FRAME_TIME_95TH]!!.size - 1]} FRAME_TIME_95TH")
        Log.d("MARCOS", "Time is ${accumulatedStats[JankStat.FRAME_TIME_99TH]!![accumulatedStats[JankStat.FRAME_TIME_99TH]!!.size - 1]} FRAME_TIME_99TH")

        ret.putInt("percentilesValue", percentileTimeInMs[percentileTimeInMs.size - 1])
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

    override fun getMetrics(): Bundle {
        val metrics = Bundle()

        // Retrieve the total number of frames
        val totals = accumulatedStats[JankStat.TOTAL_FRAMES] as List<Int>

        // Store avg, min and max of total frames
        metrics.putInt(GfxPercentileMonitor.KEY_AVG_TOTAL_FRAMES, computeAverage(totals))
        metrics.putInt(GfxPercentileMonitor.KEY_MAX_TOTAL_FRAMES, Collections.max(totals))
        metrics.putInt(GfxPercentileMonitor.KEY_MIN_TOTAL_FRAMES, Collections.min(totals))

        // Store average and max jank
        putAvgMaxDouble(
            metrics, GfxPercentileMonitor.KEY_AVG_NUM_JANKY, GfxPercentileMonitor.KEY_MAX_NUM_JANKY,
            accumulatedStats[JankStat.NUM_JANKY] as List<Double>
        )

        // Store average and max percentile frame times
        val statsFor50TH = accumulatedStats[JankStat.FRAME_TIME_50TH] as List<Int>
        if (!statsFor50TH.isEmpty()) {
            // 50th percentile frame is optional, because it wasn't available in the initial version
            // of the gfxinfo service
            putAvgMaxInteger(
                metrics, GfxPercentileMonitor.KEY_AVG_FRAME_TIME_50TH_PERCENTILE,
                GfxPercentileMonitor.KEY_MAX_FRAME_TIME_50TH_PERCENTILE, statsFor50TH
            )
        }
        putAvgMaxInteger(
            metrics, GfxPercentileMonitor.KEY_AVG_FRAME_TIME_90TH_PERCENTILE,
            GfxPercentileMonitor.KEY_MAX_FRAME_TIME_90TH_PERCENTILE,
            accumulatedStats[JankStat.FRAME_TIME_90TH] as List<Int>
        )
        putAvgMaxInteger(
            metrics, GfxPercentileMonitor.KEY_AVG_FRAME_TIME_95TH_PERCENTILE,
            GfxPercentileMonitor.KEY_MAX_FRAME_TIME_95TH_PERCENTILE,
            accumulatedStats[JankStat.FRAME_TIME_95TH] as List<Int>
        )
        putAvgMaxInteger(
            metrics, GfxPercentileMonitor.KEY_AVG_FRAME_TIME_99TH_PERCENTILE,
            GfxPercentileMonitor.KEY_MAX_FRAME_TIME_99TH_PERCENTILE,
            accumulatedStats[JankStat.FRAME_TIME_99TH] as List<Int>
        )

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