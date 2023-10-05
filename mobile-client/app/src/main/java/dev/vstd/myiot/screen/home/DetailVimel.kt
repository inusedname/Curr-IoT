package dev.vstd.myiot.screen.home

import androidx.lifecycle.ViewModel
import dev.vstd.myiot.screen.home.Singleton.rawMessage
import kotlinx.coroutines.flow.MutableStateFlow

class DetailVimel : ViewModel() {
    private val allData = MutableStateFlow(listOf<Pair<Float, Long>>())
    val uiData = MutableStateFlow(listOf<Pair<Float, Long>>())
    var increased = true

    fun setData(data: List<String>) {
        allData.value = data.map {
            val pojo = MainVimel.RemotePOJO.fromJson(it)
            pojo.temp to pojo.seconds * 1000
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