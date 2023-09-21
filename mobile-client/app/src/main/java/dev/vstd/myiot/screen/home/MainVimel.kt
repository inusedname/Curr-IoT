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
        val ledOn: Boolean = false
    )

    val state = MutableStateFlow(UiState())

    fun update(setter: (UiState) -> UiState) {
        state.value = setter(state.value)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            remoteCenter.dataFlow.collect { data ->
                rawMessage.value = rawMessage.value + (Sender.FIREBASE to data)
                println(">> Receive: $data")
                val pojo = deserialize(data)
                update {
                    it.copy(
                        temperature = pojo.temp,
                        humidity = pojo.humid
                    )
                }
            }
        }
    }

    fun toggleLed(boolean: Boolean) {
        remoteCenter.toggleLed(boolean)
        update {
            viewModelScope.launch {
                rawMessage.value += Sender.USER to "Toggle LED: $boolean"
            }
            it.copy(ledOn = boolean)
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