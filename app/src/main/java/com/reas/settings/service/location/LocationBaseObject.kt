package com.reas.settings.service.location

import android.location.Location

class LocationBaseObject(location: Location) {
    private var mLatitude: Double = location.latitude
    private var mLongitude: Double = location.longitude
    private var mAccuracy: Float = location.accuracy
    private var mSpeed: Float = location.speed
    private var mTime: Long = location.time
    private var mProvider = location.provider

    fun getLatitude(): Double {
        return this.mLatitude
    }

    fun getLongitude(): Double {
        return this.mLongitude
    }

    fun getAccuracy(): Float {
        return this.mAccuracy
    }

    fun getSpeed(): Float {
        return this.mSpeed
    }

    fun getTime(): Long {
        return this.mTime
    }

    fun getProvider(): String {
        return this.mProvider
    }
}
