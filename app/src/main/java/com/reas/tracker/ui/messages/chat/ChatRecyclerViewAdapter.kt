package com.reas.tracker.ui.messages.chat

import android.R
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reas.tracker.service.SMS.SMSBaseObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatRecyclerViewAdapter(
    private val context: Context,
    private val data: ArrayList<SMSBaseObject>
): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val smsBaseObject = data[position]

        Log.d("test", "getItemViewType: ${smsBaseObject.getDirection()} + size: ${data.toString()}")
        when(smsBaseObject.getDirection()) {
            "Incoming" -> {
                Log.d("test", "getItemViewType: incoming called")
                return VIEW_TYPE_MESSAGE_RECEIVED
            }

            "Outgoing" -> {
                Log.d("test", "getItemViewType: outgoing called")

                return VIEW_TYPE_MESSAGE_SENT
            }
        }
        Log.d("test", "getItemViewType: when failed")


        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = layoutInflater.inflate(com.reas.tracker.R.layout.recyclerview_message_received, parent, false)
            return ReceivedMessageHolder(view)
        } else if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = layoutInflater.inflate(com.reas.tracker.R.layout.recyclerview_message_sent, parent, false)
            return SentMessageHolder(view)
        }

       return SentMessageHolder(view!!)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var smsBaseObject = data[position]
        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                (holder as SentMessageHolder).bind(smsBaseObject)
            }

            VIEW_TYPE_MESSAGE_RECEIVED -> {
                (holder as ReceivedMessageHolder).bind(smsBaseObject)
            }
        }
    }


    private class ReceivedMessageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textMessageBody: TextView = itemView.findViewById<TextView>(com.reas.tracker.R.id.textMessageBody)
        val textMessageTime: TextView = itemView.findViewById<TextView>(com.reas.tracker.R.id.textMessageTime)

        fun bind(smsBaseObject: SMSBaseObject) {
            val calender = Calendar.getInstance()

            calender.timeZone = TimeZone.getDefault()
            calender.timeInMillis = smsBaseObject.getTime()

            val timeFormat = SimpleDateFormat("HH:mm")

            textMessageBody.text = smsBaseObject.mMessage
            textMessageTime.text = timeFormat.format(calender.time)
        }
    }


    private class SentMessageHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textMessageBody: TextView = itemView.findViewById<TextView>(com.reas.tracker.R.id.textMessageBody)
        val textMessageTime: TextView = itemView.findViewById<TextView>(com.reas.tracker.R.id.textMessageTime)

        fun bind(smsBaseObject: SMSBaseObject) {
            val calender = Calendar.getInstance()

            calender.timeZone = TimeZone.getDefault()
            calender.timeInMillis = smsBaseObject.getTime()

            val timeFormat = SimpleDateFormat("HH:mm")

            textMessageBody.text = smsBaseObject.mMessage
            textMessageTime.text = timeFormat.format(calender.time)
        }
    }
}

