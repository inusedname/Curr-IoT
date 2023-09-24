package dev.vstd.myiot.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import dev.vstd.myiot.screen.RemoteCenter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainVimel : ViewModel() {
    companion object {
        const val TAG = "MainVimel"
    }
    private val remoteCenter = RemoteCenter()

    val rawMessage = MutableStateFlow<List<Pair<Sender, String>>>(emptyList())
    val uiState = combine(remoteCenter.dataFlow, remoteCenter.ledFlow) { rawMessage, ledOn ->
        Log.d(TAG, "Update")
        val pojo = RemotePOJO.fromJson(rawMessage)
        UiState(
            temperature = pojo.temp,
            humidity = pojo.humid,
            lux = pojo.lux,
            time = pojo.seconds,
            ledOn = ledOn
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        remoteCenter.start()
        viewModelScope.launch {
            remoteCenter.dataFlow.collect {
                viewModelScope.launch {
                    rawMessage.value += Sender.FIREBASE to it
                }
            }
        }
    }

    fun toggleLed(boolean: Boolean = !uiState.value.ledOn) {
        remoteCenter.toggleLed(boolean)
        viewModelScope.launch {
            rawMessage.value += Sender.USER to "Toggle LED: $boolean"
        }
    }

    enum class Sender {
        FIREBASE, USER
    }

    override fun onCleared() {
        super.onCleared()
        remoteCenter.destroy()
    }

    data class UiState(
        val temperature: Float = 0f,
        val humidity: Float = 0f,
        val lux: Float = 0f,
        val time: Long = 0L,
        val ledOn: Boolean = false
    )

    data class RemotePOJO(
        val humid: Float,
        val temp: Float,
        val lux: Float,
        val seconds: Long,
    ) {
        companion object {
            fun fromJson(json: String): RemotePOJO {
                val gson = GsonBuilder().create()
                return gson.fromJson(json, RemotePOJO::class.java)
            }
        }
    }
}