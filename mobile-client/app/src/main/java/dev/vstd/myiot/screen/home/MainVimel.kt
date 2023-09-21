package dev.vstd.myiot.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import dev.vstd.myiot.screen.RemoteCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainVimel : ViewModel() {
    private val remoteCenter = RemoteCenter()

    val rawMessage = MutableStateFlow(listOf<Pair<Sender, String>>())

    enum class Sender {
        FIREBASE, USER
    }

    data class UiState(
        val temperature: Float = 0f,
        val humidity: Float = 0f,
        val time: Long = 0L,
        val ledOn: Boolean = false
    )

    val state = MutableStateFlow(UiState())
    private var buttonOn: Boolean = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            remoteCenter.getLed()
            remoteCenter.dataFlow.collect { data ->
                rawMessage.value = rawMessage.value + (Sender.FIREBASE to data)
                val pojo = deserialize(data)
                state.emit(UiState(pojo.temp, pojo.humid, pojo.seconds, buttonOn))
            }
        }
    }

    fun toggleLed(boolean: Boolean = !buttonOn) {
        remoteCenter.toggleLed(boolean)
        buttonOn = boolean
        viewModelScope.launch {
            rawMessage.value += Sender.USER to "Toggle LED: $boolean"
        }
    }

    override fun onCleared() {
        super.onCleared()
        remoteCenter.destroy()
    }

    private fun deserialize(json: String): RemotePOJO {
        val gson = GsonBuilder().create()
        return gson.fromJson(json, RemotePOJO::class.java)
    }

    data class RemotePOJO(
        val humid: Float,
        val temp: Float,
        val seconds: Long,
    )
}