package dev.vstd.myiot.data

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RemoteCenter {
    private val db = Firebase.database
    private val sensorRef = db.getReference("sensorData")
    private val ledRef = db.getReference("ledHistory")

    private val _sensorFlow = MutableSharedFlow<String>()
    val dataFlow = _sensorFlow as SharedFlow<String>
    private val _ledFlow = MutableSharedFlow<String>()
    val ledFlow = _ledFlow as SharedFlow<String>

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
    private val ledListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            GlobalScope.launch {
                _ledFlow.emit(snapshot.getValue(String::class.java)!!)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {
            Timber.e("onCancelled: ${error.code} ${error.message}")
        }
    }

    fun start() {
        sensorRef.addChildEventListener(sensorListener)
        ledRef.addChildEventListener(ledListener)
    }

    private val gson: Gson = GsonBuilder().create()

    fun toggleLed(which: Int, boolean: Boolean) {
        ledRef.push().setValue(
            gson.toJson(
                LedHistoryModel(
                    which,
                    boolean
                )
            )
        )
    }

    fun destroy() {
        sensorRef.removeEventListener(sensorListener)
        ledRef.removeEventListener(ledListener)
    }
}