package com.reas.tracker

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.telecom.Call
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import android.util.Log

class OutgoingCallHandler : CallRedirectionService() {

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        var blockCall: Boolean = false

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)

        // Number that is dialed
        var phoneNumber: String = handle.toString()
        phoneNumber = phoneNumber.replace("tel:", "")

        if (phoneNumber.equals("**2506**")) {
            // Cancels the call
            cancelCall()

            // Launches MainActivity
            startActivity(intent)

        } else {
            placeCallUnmodified()
        }
    }
}