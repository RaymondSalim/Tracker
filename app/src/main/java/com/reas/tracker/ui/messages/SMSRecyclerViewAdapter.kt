package com.reas.tracker.ui.messages

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reas.tracker.R
import com.reas.tracker.service.SMS.SMSBaseObject
import com.reas.tracker.ui.messages.chat.ChatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SMSRecyclerViewAdapter(
    private val context: Context,
    private var sortedMap: SortedMap<String, SMSBaseObject>
): RecyclerView.Adapter<SMSRecyclerViewAdapter.Holder>() {
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var listOfKeys: ArrayList<String>



    init {
        val keySet: Set<String> = sortedMap.keys
        listOfKeys = ArrayList<String>(keySet)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(layoutInflater.inflate(R.layout.recyclerview_sms, null, false))
    }

    override fun getItemCount(): Int = listOfKeys.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        listOfKeys = ArrayList<String>(sortedMap.keys)
        var smsBaseObject = sortedMap[listOfKeys[position]]
        holder.summarySMS.text = smsBaseObject!!.getBody()

        val calender = Calendar.getInstance()
        var mCurrent = listOfKeys[position]

        holder.context = context
        holder.key = listOfKeys[position]
        holder.smsSender.text = mCurrent


        calender.timeZone = TimeZone.getDefault()
        calender.timeInMillis = smsBaseObject!!.getTime()//arrays.getTime()
        calender.timeZone = TimeZone.getDefault()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        timeFormat.timeZone = TimeZone.getDefault()
        dateFormat.timeZone = TimeZone.getDefault()


        holder.date.text = dateFormat.format(calender.time)
        holder.time.text = timeFormat.format(calender.time)

        holder.itemView.setOnClickListener {
                Log.d("TAG", "onClick: clicked ${listOfKeys[position]}")
                val chatIntent = Intent(context, ChatActivity::class.java)
                chatIntent.putExtra("key", listOfKeys[position])
                context!!.startActivity(chatIntent)

        }


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


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var summarySMS: TextView
        var date: TextView
        var time: TextView
        var smsSender: TextView
        var key: String? = null
        var context: Context? = null

        init {
            with(itemView) {
                summarySMS = findViewById(R.id.smsSummary)
                date = findViewById(R.id.date)
                time = findViewById(R.id.time)
                smsSender = findViewById(R.id.smsSender)
            }

        }


    }
}