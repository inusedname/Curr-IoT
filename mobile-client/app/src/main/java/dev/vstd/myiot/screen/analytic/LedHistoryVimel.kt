package dev.vstd.myiot.screen.analytic

import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class LedHistoryVimel : ViewModel() {
    private val allData = MutableStateFlow(listOf<Triple<Int, Long, Boolean>>())
    val uiData = MutableStateFlow(listOf<Triple<Int, Long, Boolean>>())
    private var increased = false

    fun setData(data: List<String>) {
        allData.value = data.map {
            val pojo = GsonBuilder().create().fromJson(it, LedHistoryModel::class.java)
            Triple(
                when (pojo.id) {
                    "l1" -> 1
                    else -> 2
                },
                pojo.time * 1000,
                pojo.on
            )
        }
        Timber.d("allData: ${allData.value.size}")
        uiData.value = allData.value
    }

    fun filter(time: Pair<Long, Long>, which: Int? = null, on: Boolean? = null) {
        val predicate1 = { it: Triple<Int, Long, Boolean> ->
            if (which == null) true else it.first == which
        }
        val predicate2 = { it: Triple<Int, Long, Boolean> ->
            if (on == null) true else it.third == on
        }
        uiData.value = allData.value.filter {
            it.second in time.first..time.second && predicate1(it) && predicate2(it)
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