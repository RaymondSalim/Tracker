package com.reas.tracker.ui.messages

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reas.tracker.R
import com.reas.tracker.service.SMS.SMSBaseObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SMSRecyclerViewAdapter(
    private val context: Context,
    private val list: HashMap<String, ArrayList<SMSBaseObject>>
): RecyclerView.Adapter<SMSRecyclerViewAdapter.Holder>() {
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var hashmap: HashMap<String, ArrayList<SMSBaseObject>> = list
    private lateinit var listOfKeys: ArrayList<String>

    init {
        val keySet: Set<String> = hashmap.keys
        listOfKeys = ArrayList<String>(keySet)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(layoutInflater.inflate(R.layout.recyclerview_sms, null, false))
    }

    override fun getItemCount(): Int = listOfKeys.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val sorted = sortHashMap(hashmap)
        listOfKeys = ArrayList<String>(sorted.keys)
        var smsBaseObject = sorted[listOfKeys[position]]
        holder.summarySMS.text = smsBaseObject?.getBody()

        val calender = Calendar.getInstance()
        var mCurrent = listOfKeys[position]

        holder.smsSender.text = mCurrent

        calender.timeZone = java.util.TimeZone.getDefault()
        calender.timeInMillis = smsBaseObject!!.getTime()//arrays.getTime()
        calender.timeZone = java.util.TimeZone.getDefault()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        timeFormat.timeZone = java.util.TimeZone.getDefault()
        dateFormat.timeZone = java.util.TimeZone.getDefault()


        holder.date.text = dateFormat.format(calender.time)
        holder.time.text = timeFormat.format(calender.time)

        // Old code without Sort
        /**
        val calender = Calendar.getInstance()
        var mCurrent = listOfKeys[position]
        var valueArray: ArrayList<SMSBaseObject>? = hashmap[mCurrent]
        var arrays = valueArray?.get(valueArray.size - 1)

        if (arrays != null) {
            holder.summarySMS.text = arrays.getBody()
            holder.smsSender.text = mCurrent

            calender.timeZone = java.util.TimeZone.getDefault()
            calender.timeInMillis = arrays.getTime()
            calender.timeZone = java.util.TimeZone.getDefault()
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        timeFormat.timeZone = java.util.TimeZone.getDefault()
        dateFormat.timeZone = java.util.TimeZone.getDefault()

        Log.d("TAG", "onBindViewHolder: ${timeFormat.format(calender.time)}")

        holder.date.text = dateFormat.format(calender.time)
        holder.time.text = timeFormat.format(calender.time)
        **/

    }


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var summarySMS: TextView
        var date: TextView
        var time: TextView
        var smsSender: TextView

        init {
            with(itemView) {
                summarySMS = findViewById(R.id.smsSummary)
                date = findViewById(R.id.date)
                time = findViewById(R.id.time)
                smsSender = findViewById(R.id.smsSender)
            }

        }

        override fun onClick(v: View?) {
            TODO("Not yet implemented")
        }
    }

    fun sortHashMap(hashMap: HashMap<String, ArrayList<SMSBaseObject>>): SortedMap<String, SMSBaseObject> {
        val output: HashMap<String, SMSBaseObject> = HashMap<String, SMSBaseObject>()
        hashMap.forEach {
            val key = it.key
            val array = it.value
            val smsBaseObject = array[array.size - 1]
            output[key] = smsBaseObject
        }

        val sorted = output.toSortedMap(compareByDescending { output[it]?.getTime() })
        return sorted
    }
}