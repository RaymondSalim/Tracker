package com.reas.tracker

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.Telephony
import android.telecom.Call
import android.util.Log
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.reas.tracker.service.SMS.SMSBaseObject
import com.reas.tracker.service.calls.CallBaseObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var jsonUSSDDirectory: String
    lateinit var smsDirectory: String
    lateinit var callDirectory: String

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var navController: NavController

    val deviceModel = Build.MODEL
    val deviceID = Build.ID

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    val smsJsonRef = storageRef.child("users/${auth.uid}/$deviceID/SMS.json")
    val ussdJsonRef = storageRef.child("users/${auth.uid}/$deviceID/USSD.json")

    val REQUEST_ID: Int = 1;
    val permissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.MANAGE_OWN_CALLS
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jsonUSSDDirectory = getExternalFilesDir(null).toString() + "/USSDResponse.json"
        smsDirectory = getExternalFilesDir(null).toString() + "/SMS.json"
        callDirectory = getExternalFilesDir(null).toString() + "/Call.json"

        firstLaunchCheck()

        drawerLayout = findViewById(R.id.drawer_layout)
        navView= findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_responses, R.id.nav_messages, R.id.nav_calls, R.id.nav_permissions, R.id.nav_settings
            ), drawerLayout
        )
        // setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)










        val userEmail = navView.getHeaderView(0).findViewById<TextView>(R.id.userEmail)
        val userName = navView.getHeaderView(0).findViewById<TextView>(R.id.userName)


        userEmail.text = auth.currentUser!!.email
        userName.text = auth.currentUser!!.displayName ?: ""




    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ID) {
            Log.d("TAG", "onActivityResult: app is call redirection app")
        } else {
            Log.d("TAG", "onActivityResult: nope")
        }
    }

    private fun checkFiles() {
        val jsonFile = File(jsonUSSDDirectory)
        val smsFile = File(smsDirectory)
        val callFile = File (callDirectory)


        if (!smsFile.exists())  smsFile.createNewFile()
        if (!jsonFile.exists())  jsonFile.createNewFile()
        if (!callFile.exists()) callFile.createNewFile()
    }

    private fun firstLaunchCheck() {
        val sharedPreferences = getSharedPreferences("firstLaunch", Context.MODE_PRIVATE)

        if (sharedPreferences.getInt("firstLaunch", 1) == 1) {
            sharedPreferences.edit().putInt("firstLaunch", -1).apply()

            Log.d("TAG", "firstLaunchCheck: ")
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater

            builder.setView(inflater.inflate(R.layout.alertdialog, null))

            val dialog = builder.create()
            dialog.show()

            checkFiles()

            requestRole()
            requestPerms(permissions)




            dialog.dismiss()
        }
    }

    fun requestRole() {
        var roleManager: RoleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager

        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)) {
            var intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_REDIRECTION)
            startActivityForResult(intent, REQUEST_ID)
        } else {
            Toast.makeText(this, "Tracker has the role", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestPerms(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var allGranted = true

        // Checks if all permissions are granted
        for (s in grantResults ) {
            if (s == -1) {
                allGranted = false
            }
        }

        when (requestCode) {
            1 -> {

                if (grantResults.isNotEmpty() && !allGranted) {
                    Toast.makeText(this@MainActivity, "Some permissions are denied", Toast.LENGTH_SHORT).show()
                    return
                } else if (grantResults.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "Permissions Granted", Toast.LENGTH_SHORT).show()

                    loadFirebase()
                    loadSMS()
                    loadCallHistory()
                    updateFirebase()

                    return
                }
            }
        }
        return
    }

    private fun loadSMS() {
        val smsFile = File(smsDirectory)
        if (!smsFile.exists()) smsFile.createNewFile()

        val reader = JsonReader(FileReader(smsFile))

        val type = object : TypeToken<HashMap<String, ArrayList<SMSBaseObject>>>() {}.type

        var smsHashMap: HashMap<String, ArrayList<SMSBaseObject>> = Gson().fromJson(reader, type) ?: HashMap<String, ArrayList<SMSBaseObject>>()


        val contentResolver = this.contentResolver
        val cursor: Cursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,
                                            arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE),
                                            null,
                                            null,
                                            Telephony.Sms.ADDRESS) as Cursor
        var totalSms = cursor.count


        if (cursor.moveToFirst()) {
            for (i in 0 until totalSms) {

                var smsBaseObject = SMSBaseObject(cursor.getString(1), cursor.getLong(2))

                // Checks if there are existing messages from the same address
                if (smsHashMap.containsKey(cursor.getString(0))) {

                    smsHashMap.get(cursor.getString(0))?.add(smsBaseObject)

                } else {

                    var smsBaseArray = ArrayList<SMSBaseObject>()
                    smsBaseArray.add(smsBaseObject)

                    smsHashMap.put(cursor.getString(0), smsBaseArray)
                }




                cursor.moveToNext()
            }

            // Checks for duplicate
            val keySet: HashSet<String> = smsHashMap.keys.toHashSet()
            keySet.forEach { it ->
                val set: HashSet<SMSBaseObject> = HashSet<SMSBaseObject>()
                val array = smsHashMap[it]
                array!!.forEach {
                    set.add(it)
                }
                val arraylist: ArrayList<SMSBaseObject> = set.toMutableList() as ArrayList<SMSBaseObject>
                val output = arraylist.sortedBy {it ->
                    it.getTime()}.toMutableList() as ArrayList<SMSBaseObject>
                smsHashMap.replace(it, output)
            }


            val writer = FileWriter(smsFile)
            Gson().toJson(smsHashMap, writer)
            writer.close()


        } else {
            throw RuntimeException("You have no new messages")
        }
        cursor.close()

    }

    private fun loadCallHistory() {
        val callFile = File(callDirectory)
        val reader = JsonReader(FileReader(callFile))

        val type = object: TypeToken<HashMap<String, ArrayList<CallBaseObject>>>() {}.type

        var callHashMap: HashMap<String, ArrayList<CallBaseObject>> = Gson().fromJson(reader, type) ?: HashMap<String, ArrayList<CallBaseObject>>()


        val contentResolver = this.contentResolver
        val cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE, CallLog.Calls.TYPE),
            null,
            null,
            CallLog.Calls.DEFAULT_SORT_ORDER) as Cursor

        val totalCall = cursor.count

        if (cursor.moveToFirst()) {
            for (i in 0 until totalCall) {
                val number = cursor.getString(0)
                val duration = cursor.getLong(1)
                val date = cursor.getLong(2)
                var direction: String = "Unknown"

                when (cursor.getInt(3)) {
                    CallLog.Calls.INCOMING_TYPE -> {direction = "Incoming"}
                    CallLog.Calls.OUTGOING_TYPE -> {direction = "Outgoing"}
                    CallLog.Calls.MISSED_TYPE -> {direction = "Missed"}
                }

                val callBaseObject = CallBaseObject(duration, date, direction)

                // Checks if there are existing logs from the same number
                if (callHashMap.containsKey(number)) {

                    callHashMap[number]?.add(callBaseObject)

                } else {
                    var callBaseArray = ArrayList<CallBaseObject>()
                    callBaseArray.add(callBaseObject)

                    callHashMap.put(number, callBaseArray)
                }

                cursor.moveToNext()
            }

            // Checks for duplicate
            val keySet: HashSet<String> = callHashMap.keys.toHashSet()
            keySet.forEach { it ->
                val set: HashSet<CallBaseObject> = HashSet<CallBaseObject>()
                val array = callHashMap[it]
                array!!.forEach {
                    set.add(it)
                }
                val arraylist: ArrayList<CallBaseObject> = set.toMutableList() as ArrayList<CallBaseObject>
                val output = arraylist.sortedBy {it ->
                    it.getTime()}.toMutableList() as ArrayList<CallBaseObject>
                callHashMap.replace(it, output)
            }


            val writer = FileWriter(callFile)
            Gson().toJson(callHashMap, writer)
            writer.close()


        } else {
            throw RuntimeException("You have no new calls")
        }

        cursor.close()
    }


    private fun updateFirebase() {
        val storage = Firebase.storage
        val storageRef = storage.reference

        val smsJsonRef = storageRef.child("users/${auth.uid}/$deviceID/SMS.json")
        val callJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/Calls.json")


        var smsFile = Uri.fromFile(File(smsDirectory))
        var callFile = Uri.fromFile(File(callDirectory))


        smsJsonRef.putFile(smsFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase SMS File: Update Success")
        }.addOnFailureListener {
            Log.d("TAG", "updateFirebase SMS File: Failed, ${it.message}")
        }

        callJsonRef.putFile(callFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase Call File: Update Success")
        }.addOnFailureListener {
            Log.d("TAG", "updateFirebase Call File: Failed, ${it.message}")
        }
    }

    private fun loadFirebase() {
        val storage = Firebase.storage
        val storageRef = storage.reference


        val smsJsonRef = storageRef.child("users/${auth.uid}/$deviceID/SMS.json")
        val smsFile = File(smsDirectory)

        val callJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/Calls.json")
        val callFile = File(callDirectory)

        val ussdJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/USSD.json")
        val ussdFile = File(jsonUSSDDirectory)

        smsJsonRef.getFile(smsFile).addOnSuccessListener {
            Log.d("Firebase", "loadFirebase SMS File Success")
        }
            .addOnFailureListener{
                Log.d("Firebase", "loadFirebase SMS File: ${it.message}")
            }

        callJsonRef.getFile(callFile).addOnSuccessListener {
            Log.d("Firebase", "loadFirebase Call File Success")
        }
            .addOnFailureListener{
                Log.d("Firebase", "loadFirebase Call File: ${it.message}")
            }

        ussdJsonRef.getFile(ussdFile).addOnSuccessListener {
            Log.d("Firebase", "loadFirebase USSD File Success")
        }
            .addOnFailureListener{
                Log.d("Firebase", "loadFirebase USSD File: ${it.message}")
            }
    }

}





