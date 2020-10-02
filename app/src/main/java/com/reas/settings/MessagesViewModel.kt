package com.reas.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reas.settings.service.SMS.SMSBaseObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.HashMap

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    var liveData: MutableLiveData<HashMap<String, ArrayList<SMSBaseObject>>> = MutableLiveData<HashMap<String, ArrayList<SMSBaseObject>>>(loadData())
    private val sortedHashMap = sortHashMap(liveData.value!!)

    val liveSortedData: MutableLiveData<SortedMap<String, SMSBaseObject>> = MutableLiveData(sortedHashMap)



    fun getData(): MutableLiveData<HashMap<String, ArrayList<SMSBaseObject>>> {
        return liveData
    }



    private fun loadData(): java.util.HashMap<String, ArrayList<SMSBaseObject>> {
        val jsonFile = File(getApplication<Application>().applicationContext.getExternalFilesDir(null).toString() + "/SMS.json")

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

    private fun sortHashMap(hashMap: HashMap<String, ArrayList<SMSBaseObject>>): SortedMap<String, SMSBaseObject> {
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
