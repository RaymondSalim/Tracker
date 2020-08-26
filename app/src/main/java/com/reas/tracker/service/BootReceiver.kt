package com.reas.tracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.reas.tracker.service.USSD.USSDService
import com.reas.tracker.service.location.LocationService

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val ussdService = Intent(context, USSDService::class.java)
        ussdService.addFlags(Intent.FLAG_FROM_BACKGROUND)

        val locationService = Intent(context, LocationService::class.java)
        locationService.addFlags(Intent.FLAG_FROM_BACKGROUND)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(ussdService)
            context.startForegroundService(locationService)
        } else {
            context.startService(ussdService)
            context.startService(locationService)
        }
        Log.d("TAG", "onReceive: service started on boot")
    }
}