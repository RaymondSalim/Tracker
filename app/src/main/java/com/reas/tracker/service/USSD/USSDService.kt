package com.reas.tracker.service.USSD

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class USSDService : AccessibilityService() {

    var responses: ArrayList<Array<String>> = ArrayList<Array<String>>()
    private lateinit var jsonFile: File


    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= 26) {
            var CHANNEL_ID = "my_channel_01";
            var channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_MIN
            );

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            );

            var notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build();

            startForeground(1, notification);
        }
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.O)
        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            var text: String = event.text.toString()

            var dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            var localDateTime = LocalDateTime.now().format(dateTimeFormatter)

            if (event.className == "android.app.AlertDialog") {
                responses = loadJson()
                responses.add(arrayOf(localDateTime, text))
                Log.d("TAG", "onAccessibilityEvent: ${responses.last()}")
                saveResponses()
            }
        }

        override fun onServiceConnected() {
            Log.d("TAG", "onServiceConnected: USSD Accessibility Service is connected")


            var info = AccessibilityServiceInfo()
            info.flags = AccessibilityServiceInfo.DEFAULT
            info.packageNames = arrayOf("com.android.phone")
            info.eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            serviceInfo = info

            jsonFile = File(getExternalFilesDir(null).toString() + "/USSDResponse.json")

            loadJson()
        }

        fun saveResponses() {
            val writer = FileWriter(jsonFile)
            Gson().toJson(responses, writer)
            writer.close()
        }

        fun loadJson(): ArrayList<Array<String>> {
            // Loads JSON File to ArrayList<Array<String>>
            var temp = ArrayList<Array<String>>()

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
                val type = object : TypeToken<ArrayList<Array<String>>>() {}.type
                temp = Gson().fromJson<ArrayList<Array<String>>>(response, type)
            }
            return temp
        }
    }