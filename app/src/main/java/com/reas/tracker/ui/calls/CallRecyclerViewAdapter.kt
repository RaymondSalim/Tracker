package com.reas.tracker.ui.calls

import android.content.Context
import android.telecom.Call
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IntegerRes
import androidx.recyclerview.widget.RecyclerView
import com.reas.tracker.R
import com.reas.tracker.service.calls.CallBaseObject
import com.reas.tracker.ui.messages.SMSRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CallRecyclerViewAdapter(
    private val context: Context,
    private val list: HashMap<String, ArrayList<CallBaseObject>>
): RecyclerView.Adapter<CallRecyclerViewAdapter.Holder>() {
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var hashMap: HashMap<String, ArrayList<CallBaseObject>> = list
    private lateinit var listOfKeys: ArrayList<String>

    init {
        val keySet: Set<String> = hashMap.keys
        listOfKeys = ArrayList<String>(keySet)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CallRecyclerViewAdapter.Holder {
        return CallRecyclerViewAdapter.Holder(
            layoutInflater.inflate(
                R.layout.recyclerview_calls,
                null,
                false
            )
        )
    }

    override fun getItemCount(): Int = listOfKeys.size

    override fun onBindViewHolder(holder: CallRecyclerViewAdapter.Holder, position: Int) {
        val sorted: SortedMap<String, CallBaseObject> = sortHashMap(hashMap)
        listOfKeys = ArrayList<String>(sorted.keys)
        var callBaseObject: CallBaseObject = sorted[listOfKeys[position]] as CallBaseObject

        // Set the call number
        holder.callerNumber.text = listOfKeys[position]

        // Set call duration
        var duration = callBaseObject.getDuration()
        if (duration < 60) {
            holder.callDuration.text = "$duration sec"
        } else {
            val minutes = (duration / 60).toInt()
            val seconds = duration % 60
            holder.callDuration.text = "${minutes}m ${seconds}s"
        }

        // Set call direction logo
        when (callBaseObject.getDirection()) {
            "Incoming" -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_received_24) }

            "Outgoing" -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_made_24) }

            "Missed" -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_missed_24) }

            else -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_24) }
        }

        // Set call time
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getDefault()
        calendar.timeInMillis = callBaseObject.getTime()

        val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
        dateFormat.timeZone = TimeZone.getDefault()

        holder.dateTime.text = dateFormat.format(calendar.time)

    }

    private fun sortHashMap(hashMap: HashMap<String, ArrayList<CallBaseObject>>): SortedMap<String, CallBaseObject> {
        val output: HashMap<String, CallBaseObject> = HashMap<String, CallBaseObject>()
        hashMap.forEach {
            val key = it.key
            val array = it.value
            val callBaseObject = array[array.size - 1]
            output[key] = callBaseObject
        }

        return output.toSortedMap(compareByDescending { output[it]?.getTime() })
    }

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var callerNumber: TextView
        var dateTime: TextView
        var callDuration: TextView
        var callDirection: ImageView

        init {
            with (itemView) {
                callerNumber = findViewById(R.id.callNumber)
                callDuration = findViewById(R.id.duration)
                this@Holder.dateTime = findViewById(R.id.dateTime)
                callDirection = findViewById(R.id.callDirection)
            }
        }


        override fun onClick(v: View?) {
            TODO("Not yet implemented")
        }

    }

}