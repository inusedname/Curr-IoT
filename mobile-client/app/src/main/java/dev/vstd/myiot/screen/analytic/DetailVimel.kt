package dev.vstd.myiot.screen.analytic

import androidx.lifecycle.ViewModel
import dev.vstd.myiot.screen.home.MainVimel
import dev.vstd.myiot.screen.home.SensorModel
import kotlinx.coroutines.flow.MutableStateFlow

class DetailVimel : ViewModel() {
    private val allData = MutableStateFlow(listOf<Pair<Float, Long>>())
    val uiData = MutableStateFlow(listOf<Pair<Float, Long>>())
    private var increased = false

    fun setData(data: List<String>, type: Int) {
        allData.value = data.map {
            val pojo = SensorModel.fromJson(it)

            when (type) {
                TEMP -> pojo.temp
                HUMID -> pojo.humid
                LUX -> pojo.lux
                else -> throw IllegalArgumentException("Unknown type: $type")
            } to pojo.seconds * 1000
        }
        uiData.value = allData.value
    }

    fun filter(time: Pair<Long, Long>, values: Pair<Float, Float>) {
        uiData.value =
            allData.value.filter {
                it.second in time.first..time.second &&
                it.first in values.first..values.second
            }
    }

    fun sortByTime() {
        uiData.value = if (increased) {
            uiData.value.sortedBy { it.second }
        } else {
            uiData.value.sortedByDescending { it.second }
        }
        increased = !increased
    }
}