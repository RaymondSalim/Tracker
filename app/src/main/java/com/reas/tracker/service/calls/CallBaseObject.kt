package com.reas.tracker.service.calls

class CallBaseObject(duration: Long, time: Long, direction: String) {
    var mDuration: Long
    var mTime: Long
    var mDirection: String

    init {
        this.mDuration = duration
        this.mTime = time
        this.mDirection = direction
    }

    fun getDuration(): Long {
        return this.mDuration
    }

    fun getTime(): Long {
        return this.mTime
    }

    fun getDirection(): String {
        return this.mDirection
    }

    override fun hashCode(): Int {
        return this.mTime.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return !super.equals(other)
    }
}