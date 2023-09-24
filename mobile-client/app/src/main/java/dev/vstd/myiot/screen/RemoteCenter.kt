package dev.vstd.myiot.screen

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class RemoteCenter {
    private val db = Firebase.database
    private val sensorRef = db.getReference("sensorData")
    private val ledRef = db.getReference("led")

    private val _sensorFlow = MutableSharedFlow<String>()
    val dataFlow = _sensorFlow as SharedFlow<String>
    private val _ledFlow = MutableSharedFlow<Boolean>()
    val ledFlow = _ledFlow as SharedFlow<Boolean>

    @OptIn(DelicateCoroutinesApi::class)
    private val sensorListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            GlobalScope.launch {
                _sensorFlow.emit(snapshot.getValue(String::class.java)!!)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    @OptIn(DelicateCoroutinesApi::class)
    private val ledListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            GlobalScope.launch {
                _ledFlow.emit(snapshot.getValue(Boolean::class.java)!!)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    fun start() {
        sensorRef.addChildEventListener(sensorListener)
        ledRef.addValueEventListener(ledListener)
    }

    fun toggleLed(boolean: Boolean) {
        ledRef.setValue(boolean)
    }

    fun destroy() {
        sensorRef.removeEventListener(sensorListener)
        ledRef.removeEventListener(ledListener)
    }
}