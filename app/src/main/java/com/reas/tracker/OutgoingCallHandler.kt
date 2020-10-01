package com.reas.tracker

import android.content.Intent
import android.net.Uri
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import android.util.Log

class OutgoingCallHandler : CallRedirectionService() {
    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        Log.d("LMAO", "onPlaceCall: Service is on")

        val intent = Intent(baseContext, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Number that is dialed
        var phoneNumber: String = handle.toString()
        phoneNumber = phoneNumber.replace("tel:", "")

        if (phoneNumber == "**2506**") {
            // Launches MainActivity
            Log.d("LMAO", "onPlaceCall: ")
            startActivity(intent)

            // Cancels the call
            cancelCall()


        } else {
            placeCallUnmodified()
        }
    }


}