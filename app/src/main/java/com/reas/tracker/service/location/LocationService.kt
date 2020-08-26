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


class LocationService: Service() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocation: Location? = null
    private var jsonFile: File? = null
    private var locationDirectory: String? = null
    private var locationArray: ArrayList<LocationBaseObject> = ArrayList<LocationBaseObject>()



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

        createLocationRequest()
        getLastLocation()


    }

    private fun onNewLocation(lL: Location) {
        var location = LocationBaseObject(lL)
        jsonFile = File(locationDirectory)
        locationArray = loadJson()
        locationArray.add(location)
        Log.d("TAG", "onNewLocation: $locationArray")
        saveResponses()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1200000
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
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        Log.d("TAG", "location update $location")
                        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/${Build.DEVICE}")
                        ref.addValueEventListener(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                Log.d("Firebase Database", "onCancelled: Database not updated $error")
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.d("Firebase Database", "onDataChange: Database updated")
                            }

                        })
                        ref.setValue(location)
                        onNewLocation(location)

                    }
                }
            }, null)
        }

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
        // Loads JSON File to ArrayList<SMSObject>
        var temp = ArrayList<LocationBaseObject>()

        val fileReader = FileReader(jsonFile)
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
            temp = Gson().fromJson<ArrayList<LocationBaseObject>>(response, type)
        }
        return temp
    }

    private fun saveResponses() {
        val writer = FileWriter(jsonFile)
        Gson().toJson(locationArray, writer)
        writer.close()

        updateFirebase()
    }

    private fun updateFirebase() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val deviceModel = Build.MODEL
        val deviceID = Build.ID
        val auth = FirebaseAuth.getInstance()

        val locationJsonRef = storageRef.child("users/${auth.uid}/$deviceID/Location.json")

        var file = Uri.fromFile(jsonFile)

        val uploadTask = locationJsonRef.putFile(file)
        uploadTask.addOnFailureListener {
            locationJsonRef.putFile(file)
        }
    }
}