package dev.vstd.myiot.screen.home

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dev.vstd.myiot.R
import kotlin.random.Random

class TempHumidChart(context: Context) {
    private val appTypeface = ResourcesCompat.getFont(context, R.font.poppins)
    private val contentColor = Color.DKGRAY
    val view = LineChart(context)

    init {
        setup()
    }

    fun addData(humid: Float, temp: Float, lux: Float) {
        val data = view.data
        val humidSet = data.getDataSetByIndex(1)
        val tempSet = data.getDataSetByIndex(0)
        val luxSet = data.getDataSetByIndex(2)
        val dustSet = data.getDataSetByIndex(3)
        humidSet.addEntry(Entry(humidSet.entryCount.toFloat(), humid))
        tempSet.addEntry(Entry(tempSet.entryCount.toFloat(), temp))
        luxSet.addEntry(Entry(luxSet.entryCount.toFloat(), lux))
        dustSet.addEntry(Entry(dustSet.entryCount.toFloat(), Random.nextInt(0, 100).toFloat()))
        data.notifyDataChanged()
        view.notifyDataSetChanged()
        view.invalidate()
    }

    private fun setup() {
        view.setScaleEnabled(false)
        view.description = null
        view.apply {
            xAxis.isEnabled = false
            axisLeft.apply {
                enableGridDashedLine(10f, 10f, 0f)
                gridLineWidth = 1f
            }
            axisRight.isEnabled = false
        }
        view.data = LineData(
            LineDataSet(null, "Temp").applyStyleDataSet(Color.RED),
            LineDataSet(null, "Humid").applyStyleDataSet(Color.BLUE),
            LineDataSet(null, "Lux").applyStyleDataSet(Color.GREEN),
            LineDataSet(null, "Dust").applyStyleDataSet(Color.YELLOW)
        )
    }

    private fun LineDataSet.applyStyleDataSet(color: Int): LineDataSet {
        isHighlightEnabled = false
        valueTextSize = 12f
        valueTypeface = appTypeface
        valueTextColor = contentColor
        setColor(color)
        setDrawValues(false)
        setDrawCircles(false)
        setDrawCircleHole(false)
        return this
    }
}