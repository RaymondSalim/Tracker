package com.reas.tracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.reas.tracker.service.USSD.USSDService

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val intent = Intent(context, USSDService::class.java)
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
        Log.d("TAG", "onReceive: service started on boot")
    }
}