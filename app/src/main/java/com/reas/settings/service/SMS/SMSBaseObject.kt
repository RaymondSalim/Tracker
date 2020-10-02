package com.reas.settings.service.SMS

class SMSBaseObject(message: String, time: Long, direction: String) {
    var mMessage: String
    var mTime: Long = 0
    var mDirection: String

    init {
        this.mMessage = message
        this.mTime = time
        this.mDirection = direction
    }

    fun getBody(): String {
        return mMessage
    }

    fun getTime(): Long {
        return mTime
    }

    fun getDirection(): String {
        return mDirection
    }

    override fun hashCode(): Int {
        return this.mTime.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return !super.equals(other)
    }
}