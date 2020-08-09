package com.reas.tracker.service.SMS

import com.google.gson.JsonObject

class SMSBaseObject(message: String, time: Long) {
    var mMessage: String
    var mTime: Long = 0

    init {
        this.mMessage = message
        this.mTime = time
    }

    fun getBody(): String {
        return mMessage
    }

    fun getTime(): Long {
        return mTime
    }
}