package com.reas.tracker

import android.Manifest
import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.Telephony
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.ui.AppBarConfiguration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.reas.tracker.service.SMS.SMSBaseObject
import com.reas.tracker.service.USSD.USSDService
import com.reas.tracker.service.calls.CallBaseObject
import com.reas.tracker.service.location.LocationService
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var jsonUSSDDirectory: String
    lateinit var smsDirectory: String
    lateinit var callDirectory: String
    lateinit var locationDirectory: String
    lateinit var conversationDirectory: String


    val deviceDevice = Build.DEVICE

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private val REQUEST_ID: Int = 1
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
    private val drawOverApp = arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW)

    private val dialog = LoadingDialogFragment()

    var fileLoaded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jsonUSSDDirectory = getExternalFilesDir(null).toString() + "/USSDResponse.json"
        smsDirectory = getExternalFilesDir(null).toString() + "/SMS.json"
        callDirectory = getExternalFilesDir(null).toString() + "/Call.json"
        locationDirectory = getExternalFilesDir(null).toString() + "/Location.json"
        conversationDirectory = getExternalFilesDir(null).toString() + "/Conversation.json"


        firstLaunchCheck()

        val permissionsFragment = PermissionsFragment()
        supportFragmentManager.beginTransaction().add(R.id.content_main, permissionsFragment, "permissionsFragment").commit()


        starServices()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
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
        val callFile = File(callDirectory)
        val locationFile = File(locationDirectory)
        val convFile = File(conversationDirectory)


        if (!smsFile.exists())  smsFile.createNewFile()
        if (!jsonFile.exists())  jsonFile.createNewFile()
        if (!callFile.exists()) callFile.createNewFile()
        if (!locationFile.exists()) locationFile.createNewFile()
        if (!convFile.exists()) convFile.createNewFile()
    }

    private fun firstLaunchCheck() {
        val sharedPreferences = getSharedPreferences("firstLaunch", Context.MODE_PRIVATE)

        if (sharedPreferences.getInt("firstLaunch", 1) == 1) {
            sharedPreferences.edit().putInt("firstLaunch", -1).apply()

            Log.d("TAG", "firstLaunchCheck: ")

            checkFiles()

            requestRole()
            requestPerms(permissions)

        } else { val model: MessagesViewModel by viewModels() }
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
        ActivityCompat.requestPermissions(this@MainActivity, drawOverApp, 0)
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


                    dialog.show(supportFragmentManager, "dialog")

                    loadFirebase()

                }
            }
        }
    }

    private fun loadFirebase() {
        Log.d("TAG", "loadFirebase: start")
        val storage = Firebase.storage
        val storageRef = storage.reference


        val smsJsonRef = storageRef.child("users/${auth.uid}/$deviceDevice/SMS.json")
        val smsFile = File(smsDirectory)

        val callJsonRef = storageRef.child("users/${auth.uid}/${deviceDevice}/Calls.json")
        val callFile = File(callDirectory)

        val ussdJsonRef = storageRef.child("users/${auth.uid}/${deviceDevice}/USSD.json")
        val ussdFile = File(jsonUSSDDirectory)

        val locationJsonRef = storageRef.child("users/${auth.uid}/${deviceDevice}/Location.json")
        val locationFile = File(locationDirectory)


        var fileDownloaded = 0

        smsJsonRef.getFile(smsFile).addOnSuccessListener {
            Log.d("Firebase", "loadFirebase SMS File Success")
        }
            .addOnFailureListener{
                Log.d("Firebase", "loadFirebase SMS File: ${it.message}")
            }
            .addOnCompleteListener {

                if (fileDownloaded < 3) {
                    fileDownloaded++
                } else {
                    loadFiles()
                }
                Log.d("yee", "$fileDownloaded")
            }


        callJsonRef.getFile(callFile).addOnSuccessListener {
            Log.d("Firebase", "loadFirebase Call File Success")
        }
            .addOnFailureListener{
                Log.d("Firebase", "loadFirebase Call File: ${it.message}")
            }
            .addOnCompleteListener {

                if (fileDownloaded < 3) {
                    fileDownloaded++
                } else {
                    loadFiles()
                }
                Log.d("yee", "$fileDownloaded")

            }


        ussdJsonRef.getFile(ussdFile).addOnSuccessListener {
            Log.d("Firebase", "loadFirebase USSD File Success")
        }
            .addOnFailureListener{
                Log.d("Firebase", "loadFirebase USSD File: ${it.message}")
            }
            .addOnCompleteListener {

                if (fileDownloaded < 3) {
                    fileDownloaded++
                } else {
                    loadFiles()
                }
                Log.d("yee", "$fileDownloaded")

            }

        locationJsonRef.getFile(locationFile).addOnSuccessListener {
            Log.d("Firebase", "loadFirebase Location File Success")
        }
            .addOnFailureListener{
                Log.d("Firebase", "loadFirebase Location File: ${it.message}")
            }
            .addOnCompleteListener {

                if (fileDownloaded < 3) {
                    fileDownloaded++
                } else {
                    loadFiles()
                }
                Log.d("yee", "$fileDownloaded")

            }

    }

    private fun updateFirebase() {
        Log.d("TAG", "updateFirebase: start")
        val storage = Firebase.storage
        val storageRef = storage.reference

        val smsJsonRef = storageRef.child("users/${auth.uid}/$deviceDevice/SMS.json")
        val callJsonRef = storageRef.child("users/${auth.uid}/${deviceDevice}/Calls.json")
        val conversationJsonRef = storageRef.child("users/${auth.uid}/$deviceDevice/Conversation.json")



        var smsFile = Uri.fromFile(File(smsDirectory))
        var callFile = Uri.fromFile(File(callDirectory))
        var convFile = Uri.fromFile(File(conversationDirectory))

        smsJsonRef.putFile(smsFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase SMS File: Update Success")
        }.addOnFailureListener {
            Log.d("TAG", "updateFirebase SMS File: Failed, ${it.message}")
        }

        callJsonRef.putFile(callFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase Call File: Update Success")
            dialog.dismiss()

        }.addOnFailureListener {
            Log.d("TAG", "updateFirebase Call File: Failed, ${it.message}")
        }

        conversationJsonRef.putFile(convFile).addOnSuccessListener {
            Log.d("Firebase", "updateFirebase Conversation File: Update Success")
            dialog.dismiss()

        }.addOnFailureListener {
            Log.d("TAG", "updateFirebase Conversation File: Failed, ${it.message}")
        }
        Log.d("TAG", "updateFirebase: end")
    }

    private fun loadFiles() {
        Log.d("TAG", "loadFiles: ")
        val loadSMS = LoadSMS(context = this@MainActivity)
        loadSMS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null)

        val loadCallHistory = LoadCallHistory(context = this@MainActivity)
        loadCallHistory.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null)

    }

    private fun starServices() {
        val locationService = Intent(applicationContext, LocationService::class.java)
        locationService.addFlags(Intent.FLAG_FROM_BACKGROUND)

        val ussdService = Intent(applicationContext, USSDService::class.java)
        ussdService.addFlags(Intent.FLAG_FROM_BACKGROUND)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!(isMyServiceRunning(LocationService::class.java))) {
                applicationContext.startForegroundService(locationService)
            }
            if (!(isMyServiceRunning(USSDService::class.java))) {
                applicationContext.startForegroundService(ussdService)
            }

        } else {
            if (!(isMyServiceRunning(LocationService::class.java))) {
                applicationContext.startService(locationService)
            }
            if (!(isMyServiceRunning(USSDService::class.java))) {
                applicationContext.startService(ussdService)
            }
        }
    }

    //TODO! Find replacement
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private class LoadSMS(context: MainActivity): AsyncTask<Void, Void, Int>() {
        var activityReference: WeakReference<MainActivity>? = null
        init {
         activityReference = WeakReference(context)
        }

        override fun doInBackground(vararg params: Void): Int {

            val smsFile = File(activityReference!!.get()!!.smsDirectory)
            if (!smsFile.exists()) smsFile.createNewFile()

            val reader = JsonReader(FileReader(smsFile))

            val type = object : TypeToken<HashMap<String, ArrayList<SMSBaseObject>>>() {}.type

            var smsHashMap: HashMap<String, ArrayList<SMSBaseObject>> =
                Gson().fromJson(reader, type) ?: HashMap<String, ArrayList<SMSBaseObject>>()


            val contentResolver = (activityReference!!.get()!!).contentResolver

            // Incoming messages
            val cursorIncoming: Cursor = contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.Inbox.ADDRESS,
                    Telephony.Sms.Inbox.BODY,
                    Telephony.Sms.Inbox.DATE
                ),
                null,
                null,
                Telephony.Sms.ADDRESS
            ) as Cursor
            var totalIncoming = cursorIncoming.count
            cursorIncoming.columnNames.forEach {
                Log.d("TAG", "LoadSMS: $it")
            }


            if (cursorIncoming.moveToFirst()) {
                for (i in 0 until totalIncoming) {

                    var smsBaseObject =
                        SMSBaseObject(cursorIncoming.getString(1), cursorIncoming.getLong(2), "Incoming")

                    // Checks if there are existing messages from the same address
                    if (smsHashMap.containsKey(cursorIncoming.getString(0))) {

                        smsHashMap[cursorIncoming.getString(0)]?.add(smsBaseObject)

                    } else {

                        var smsBaseArray = ArrayList<SMSBaseObject>()
                        smsBaseArray.add(smsBaseObject)

                        smsHashMap[cursorIncoming.getString(0)] = smsBaseArray
                    }




                    cursorIncoming.moveToNext()
                }
                cursorIncoming.close()
            } else {
                throw RuntimeException("You have no new messages")
            }

            // Outgoing messages
            val cursorOutgoing: Cursor = contentResolver.query(
                Telephony.Sms.Sent.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.Inbox.ADDRESS,
                    Telephony.Sms.Inbox.BODY,
                    Telephony.Sms.Inbox.DATE
                ),
                null,
                null,
                Telephony.Sms.ADDRESS
            ) as Cursor
            var totalOutgoing = cursorOutgoing.count

            if (cursorOutgoing.moveToFirst()) {
                for (i in 0 until totalOutgoing) {

                    var smsBaseObject =
                        SMSBaseObject(cursorOutgoing.getString(1), cursorOutgoing.getLong(2), "Outgoing")

                    // Checks if there are existing messages from the same address
                    if (smsHashMap.containsKey(cursorOutgoing.getString(0))) {

                        smsHashMap.get(cursorOutgoing.getString(0))?.add(smsBaseObject)

                    } else {

                        var smsBaseArray = ArrayList<SMSBaseObject>()
                        smsBaseArray.add(smsBaseObject)

                        smsHashMap.put(cursorOutgoing.getString(0), smsBaseArray)
                    }




                    cursorOutgoing.moveToNext()
                }
                cursorOutgoing.close()
            } else {
                throw RuntimeException("You have no new messages")
            }

            // Checks for duplicate
            val keySet: HashSet<String> = smsHashMap.keys.toHashSet()
            keySet.forEach { it ->
                val set: HashSet<SMSBaseObject> = HashSet<SMSBaseObject>()
                val array = smsHashMap[it]
                array!!.forEach {
                    set.add(it)
                }
                val arraylist: ArrayList<SMSBaseObject> =
                    set.toMutableList() as ArrayList<SMSBaseObject>
                val output = arraylist.sortedBy { it ->
                    it.getTime()
                }.toMutableList() as ArrayList<SMSBaseObject>
                smsHashMap.replace(it, output)
            }

            val output: HashMap<String, SMSBaseObject> = HashMap()
            smsHashMap.forEach {
                val key = it.key
                val array = it.value
                val smsBaseObject = array[array.size - 1]
                output[key] = smsBaseObject
            }

            val conversation = output.toSortedMap(compareByDescending { output[it]?.getTime() })
            val convFile = File(activityReference!!.get()!!.conversationDirectory)
            val convWriter = FileWriter(convFile)
            Gson().toJson(conversation, convWriter)
            convWriter.close()


            val writer = FileWriter(smsFile)
            Gson().toJson(smsHashMap, writer)
            writer.close()



            return 1
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            val activity = activityReference!!.get()
            Log.d("TAG", "onPostExecutesms: ${activity!!.fileLoaded}")

            if ((activity == null) or activity.isFinishing) return
            if (activity.fileLoaded < 1) {
                activity.fileLoaded++
            } else activity.updateFirebase()
        }
    }

    private class LoadCallHistory(context: MainActivity): AsyncTask<Void, Void, Int>() {
        var activityReference: WeakReference<MainActivity>? = null
        init {
            activityReference = WeakReference(context)
        }

        override fun doInBackground(vararg params: Void?): Int {
            val callFile = File((activityReference!!.get()!!).callDirectory)
            val reader = JsonReader(FileReader(callFile))

            val type = object: TypeToken<HashMap<String, ArrayList<CallBaseObject>>>() {}.type

            var callHashMap: HashMap<String, ArrayList<CallBaseObject>> = Gson().fromJson(reader, type) ?: HashMap<String, ArrayList<CallBaseObject>>()


            val contentResolver = (activityReference!!.get()!!).contentResolver


            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE, CallLog.Calls.TYPE),
                null,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER
            ) as Cursor

            val totalCall = cursor.count

            if (cursor.moveToFirst()) {
                for (i in 0 until totalCall) {
                    val number = cursor.getString(0)
                    val duration = cursor.getLong(1)
                    val date = cursor.getLong(2)
                    var direction: String = "Unknown"

                    when (cursor.getInt(3)) {
                        CallLog.Calls.INCOMING_TYPE -> {
                            direction = "Incoming"
                        }
                        CallLog.Calls.OUTGOING_TYPE -> {
                            direction = "Outgoing"
                        }
                        CallLog.Calls.MISSED_TYPE -> {
                            direction = "Missed"
                        }
                    }

                    val callBaseObject = CallBaseObject(duration, date, direction)

                    // Checks if there are existing logs from the same number
                    if (callHashMap.containsKey(number)) {

//                    callHashMap[number]?.add(callBaseObject)
                        callHashMap.get(number)!!.add(callBaseObject)

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
                    val output = arraylist.sortedBy { it ->
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
            Log.d("TAG", "LoadCallHistory: end")
            return 1
        }

        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)
            val activity = activityReference!!.get()
            Log.d("TAG", "onPostExecutecall: ${activity!!.fileLoaded}")

            if ((activity == null) or activity.isFinishing) return
            if (activity.fileLoaded < 1) {
                activity.fileLoaded++
            } else activity.updateFirebase()
        }
    }
}





