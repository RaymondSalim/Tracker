package com.reas.tracker.ui.messages.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reas.tracker.R
import com.reas.tracker.service.SMS.SMSBaseObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class ChatActivity : AppCompatActivity() {
    private lateinit var chatRecyclerViewAdapter: ChatRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var data: HashMap<String, ArrayList<SMSBaseObject>>
    private lateinit var jsonFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent = intent
        val key = intent.getStringExtra("key")
        Log.d("test", "onCreate: ${key}")

        jsonFile = File(this@ChatActivity.getExternalFilesDir(null).toString() + "/SMS.json")
        data = loadJson()

        recyclerView = findViewById(R.id.chatRecyclerView)
        chatRecyclerViewAdapter = ChatRecyclerViewAdapter(this@ChatActivity, data[key]!!)
        recyclerView.adapter = chatRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    private fun loadJson(): java.util.HashMap<String, ArrayList<SMSBaseObject>> {
        // Loads JSON File to ArrayList<SMSObject>
        var temp = java.util.HashMap<String, ArrayList<SMSBaseObject>>()

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
            val type = object : TypeToken<java.util.HashMap<String, ArrayList<SMSBaseObject>>>() {}.type
            temp = Gson().fromJson<java.util.HashMap<String, ArrayList<SMSBaseObject>>>(response, type)
        }
        return temp
    }
}