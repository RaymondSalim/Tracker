package com.reas.tracker.service.SMS

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class SMSBroadcastReceiver: BroadcastReceiver() {
    var smsMessages = HashMap<String, ArrayList<SMSBaseObject>>()
    lateinit var jsonFile: File


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val smsDirectory = context.getExternalFilesDir(null).toString() + "/SMS.json"

        jsonFile = File(smsDirectory)

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

                        var smsBaseObject = SMSBaseObject(msgBody, msgTime)

                        // Checks if there are existing messages from the same address
                        if (smsMessages.containsKey(msgFrom)) {

                            smsMessages.get(msgFrom)?.add(smsBaseObject)

                        } else {

                            var smsBaseArray = ArrayList<SMSBaseObject>()
                            smsBaseArray.add(smsBaseObject)

                            smsMessages.put(msgFrom, smsBaseArray)
                        }


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

    fun saveResponses() {
        val writer = FileWriter(jsonFile)
        Gson().toJson(smsMessages, writer)
        writer.close()
    }

    }
