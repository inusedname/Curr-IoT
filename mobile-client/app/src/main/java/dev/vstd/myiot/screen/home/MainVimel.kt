package dev.vstd.myiot.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.vstd.myiot.screen.RemoteCenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainVimel: ViewModel() {
    private val remoteCenter = RemoteCenter()

    private val _rawMessage = MutableStateFlow(listOf<String>())
    val rawMessage = _rawMessage.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            remoteCenter.dataFlow.collect {
                _rawMessage.value = _rawMessage.value + it
            }
        }
    }

    fun toggleLed(boolean: Boolean) {
        remoteCenter.toggleLed(boolean)
    }

    override fun onCleared() {
        super.onCleared()
        remoteCenter.destroy()
    }
}