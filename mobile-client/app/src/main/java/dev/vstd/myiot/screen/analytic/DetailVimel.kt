package dev.vstd.myiot.screen.analytic

import androidx.lifecycle.ViewModel
import dev.vstd.myiot.screen.home.SensorModel
import kotlinx.coroutines.flow.MutableStateFlow

class DetailVimel : ViewModel() {
    private val allData = MutableStateFlow(listOf<SensorModel>())
    val uiData = MutableStateFlow(listOf<SensorModel>())
    private var increased = false

    fun setData(data: List<String>) {
        allData.value = data.map {
            SensorModel.fromJson(it)
        }
        uiData.value = allData.value
    }

    fun filter(time: Pair<Long, Long>, values: Triple<Type?, Float, Float>) {
        uiData.value =
            allData.value.filter {
                (it.seconds * 1000) in time.first..time.second
            }.filter {
                when (values.first) {
                    Type.HUMID -> it.humid in values.second..values.third
                    Type.TEMP -> it.temp in values.second..values.third
                    Type.LUX -> it.lux in values.second..values.third
                    else -> true
                }
            }
    }

    fun sortBy(option: Type) {
        uiData.value = if (increased) {
            uiData.value.sortedBy {
                when (option) {
                    Type.HUMID -> it.humid
                    Type.TEMP -> it.temp
                    Type.LUX -> it.lux
                    Type.TIME -> it.seconds.toFloat()
                }
            }
        } else {
            uiData.value.sortedByDescending {
                when (option) {
                    Type.HUMID -> it.humid
                    Type.TEMP -> it.temp
                    Type.LUX -> it.lux
                    Type.TIME -> it.seconds.toFloat()
                }
            }
        }
        increased = !increased
    }

    enum class Type {
        TIME,
        HUMID,
        TEMP,
        LUX
    }
}