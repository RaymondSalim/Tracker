package com.reas.tracker.service.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

private const val TAG = "LocationService"

class LocationService: Service() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocation: Location? = null
    private var locationFile: File? = null
    private var locationDirectory: String? = null
    private var locationArray: ArrayList<LocationBaseObject> = ArrayList()

    private var mInterval = 120000L

//    private var ref: DatabaseReference? = null


    override fun onCreate() {
        locationDirectory = applicationContext.getExternalFilesDir(null).toString() + "/Location.json"
        super.onCreate()
        Log.d("TAG", "onCreate: location service started")
        buildNotification()
        loginToFirebase()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult!!.lastLocation)
            }
        }

        getInterval()
        createLocationRequest()
        getLastLocation()


    }

    private fun onNewLocation(lL: Location) {
        var location = LocationBaseObject(lL)
        locationFile = File(locationDirectory)
        locationArray = loadJson()

//        // Gets the milliseconds time of the date
//        val currentDate = (Date().time - (Date().time % 86400000L))
//
//        if (locationMap[currentDate] != null) {
//            val array = locationMap[currentDate]!!
//            array.add(location)
//            locationMap[currentDate] = array
//
//        } else {
//            val array = ArrayList<LocationBaseObject>()
//            array.add(location)
//            locationMap[currentDate] = array
//        }

        locationArray.add(location)

        saveResponses()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = mInterval
        mLocationRequest!!.fastestInterval = mLocationRequest!!.interval / 2
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            mFusedLocationClient!!.lastLocation.addOnCompleteListener {
//                if (it.isSuccessful && it.result != null) {
//                    mLocation = it.result
//                } else {
//                    Log.w("TAG", "getLastLocation: Failed")
//                }
//            }
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, callback, null)
        }

    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            if (location != null) {
                Log.d("TAG", "location update $location")
                val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/location/${Build.DEVICE}")
//                ref.addValueEventListener(object: ValueEventListener {
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.d("Firebase Database", "onCancelled: Database not updated $error")
//                    }
//
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        Log.d("Firebase Database", "onDataChange: Database updated")
//                    }
//
//                })
                ref.setValue(location)
                onNewLocation(location)

            }
        }
    }

    private fun getInterval() {
        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/settings/${Build.DEVICE}/location")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mInterval = snapshot.getValue(Long::class.java)!!
                Log.d(TAG, "onDataChange: Interval: $mInterval")
                mFusedLocationClient?.removeLocationUpdates(mLocationCallback)

                createLocationRequest()
                getLastLocation()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var CHANNEL_ID = "my_channel_01";
            var channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_MIN
            );

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            );

            var notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build();

            startForeground(1, notification);
        }
    }

    private fun loginToFirebase() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            Log.d("TAG", "loginToFirebase: Firebase not signed in")
            auth.signInAnonymously()
        } else {
            Log.d("TAG", "loginToFirebase: Firebase is signed in to ${auth.currentUser!!.email}")
        }
    }

    private fun loadJson(): ArrayList<LocationBaseObject> {
        // Loads JSON File to ArrayList<LocationBaseObject>
        var temp = ArrayList<LocationBaseObject>()

        val fileReader = FileReader(locationFile)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()
        val response = stringBuilder.toString()

        if (response != "") {
            val type = object : TypeToken<ArrayList<LocationBaseObject>>() {}.type
            temp = Gson().fromJson(response, type)
        }
        return temp
    }

    private fun saveResponses() {
        val writer = FileWriter(locationFile)
        Gson().toJson(locationArray, writer)
        writer.close()

        updateFirebase()
    }

    private fun updateFirebase() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val deviceDevice = Build.DEVICE
        val auth = FirebaseAuth.getInstance()

        val locationJsonRef = storageRef.child("users/${auth.uid}/$deviceDevice/Location.json")

        var file = Uri.fromFile(locationFile)

        val uploadTask = locationJsonRef.putFile(file)
        uploadTask.
        addOnCompleteListener{
            Log.d("LocationService", "updateFirebase: complete")
        }.addOnFailureListener {
            Log.d("LocationService", "updateFirebase: Failed")
            locationJsonRef.putFile(file)
        }
    }
}