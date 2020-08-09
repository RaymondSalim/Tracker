package com.reas.tracker.ui.USSD

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.reas.tracker.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

class USSDRecyclerViewAdapter(
    private val context: Context,
    private val list:ArrayList<Array<String>>
) : RecyclerView.Adapter<USSDRecyclerViewAdapter.Holder>() {
    lateinit var layoutInflater: LayoutInflater
    lateinit var data: ArrayList<Array<String>>

    init {
        layoutInflater = LayoutInflater.from(context)
        data = list
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): USSDRecyclerViewAdapter.Holder {
        return Holder((layoutInflater.inflate(R.layout.recyclerview_ussd, parent, false)))
    }

    override fun getItemCount(): Int = data.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: USSDRecyclerViewAdapter.Holder, position: Int) {
        var mCurrent = data.get(position)
        holder.summaryUSSD.text = mCurrent[1]
        var date = mCurrent[0]
        var time: String = ""

        if (mCurrent.isNotEmpty()) {
            var ldt: LocalDateTime = LocalDateTime.parse(date)
            var dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
            date = dateFormatter.format(ldt)

            var timeFormatter = DateTimeFormatter.ofPattern("HH:m:s")
            time = timeFormatter.format(ldt)
        }

        holder.date.text = date
        holder.time.text = time

    }


    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var summaryUSSD: TextView
        var date: TextView
        var time: TextView

        init {
            with(itemView) {
                summaryUSSD = findViewById(R.id.ussdSummary)
                date = findViewById(R.id.date)
                time = findViewById(R.id.time)
            }
        }


        override fun onClick(v: View?) {
            TODO("Not yet implemented")
        }

    }


}





