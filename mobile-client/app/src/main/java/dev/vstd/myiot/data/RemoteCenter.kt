package dev.vstd.myiot.data

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RemoteCenter {
    private val db = Firebase.database
    private val sensorRef = db.getReference("sensorData")
    private val ledRef = db.getReference("status")

    private val _sensorFlow = MutableSharedFlow<String>()
    val dataFlow = _sensorFlow as SharedFlow<String>
    private val _ledFlow = MutableSharedFlow<Pair<String, Boolean>>()
    val ledFlow = _ledFlow as SharedFlow<Pair<String, Boolean>>

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
        override fun onCancelled(error: DatabaseError) {
            Timber.e("onCancelled: ${error.code} ${error.message}")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private val ledListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            GlobalScope.launch {
                val key = snapshot.key!!
                val on = snapshot.child("on").getValue(Boolean::class.java)!!
                _ledFlow.emit(key to on)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            GlobalScope.launch {
                Timber.d("Receive!: ${snapshot.key} ${snapshot.value}")
                val key = snapshot.key!!
                val on = snapshot.child("on").getValue(Boolean::class.java)!!
                _ledFlow.emit(key to on)
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {
            Timber.e("onCancelled: ${error.code} ${error.message}")
        }
    }

    fun getLedHistorySnapshot(onComplete: (List<String>) -> Unit) {
        val ref = db.getReference("ledHistory")
        val list = mutableListOf<String>()
        ref.get().addOnSuccessListener {
            it.children.forEach { snap ->
                list.add(snap.getValue(String::class.java)!!)
            }
            Timber.d(list.size.toString())
            onComplete(list)
        }
    }

    fun start() {
        sensorRef.addChildEventListener(sensorListener)
        ledRef.addChildEventListener(ledListener)
    }

    fun toggleLed(which: Int, boolean: Boolean) {
        Timber.d("toggleLed: $which $boolean")
        ledRef.child(
            when (which) {
                1 -> "l1"
                else -> "l2"
            }
        ).child("on").setValue(boolean)
    }

    fun destroy() {
        sensorRef.removeEventListener(sensorListener)
        ledRef.removeEventListener(ledListener)
    }
}