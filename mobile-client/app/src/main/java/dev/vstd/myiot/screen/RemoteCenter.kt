package dev.vstd.myiot.screen

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RemoteCenter {
    private val db = Firebase.database
    private val sensorRef = db.getReference("sensorData")
    private val ledRef = db.getReference("led")

    private val _dataFlow = MutableSharedFlow<String>()
    val dataFlow = _dataFlow as SharedFlow<String>

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            GlobalScope.launch {
                _dataFlow.emit(snapshot.getValue(String::class.java)!!)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    init {
        sensorRef.addChildEventListener(listener)
    }

    fun toggleLed(boolean: Boolean) {
        ledRef.setValue(boolean)
    }

    fun destroy() {
        sensorRef.removeEventListener(listener)
    }

    suspend fun getLed() : Boolean {
        return coroutineScope {
            ledRef.get().await().getValue(Boolean::class.java)!!
        }
    }
}

data class SensorData(
    val dhtHumid: Float,
    val dhtTemp: Float,
    val timestamp: Long,
)