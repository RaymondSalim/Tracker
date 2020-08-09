package com.reas.tracker

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.Menu
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
import com.google.gson.Gson
import com.reas.tracker.service.SMS.SMSBaseObject
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var jsonUSSDDirectory: String
    lateinit var smsDirectory: String

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var navController: NavController


    val REQUEST_ID: Int = 1;
    val permissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.FOREGROUND_SERVICE
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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







        jsonUSSDDirectory = getExternalFilesDir(null).toString() + "/USSDResponse.json"
        smsDirectory = getExternalFilesDir(null).toString() + "/SMS.json"

        firstLaunchCheck()









    }

    override fun onStart() {
        super.onStart()


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
                    loadSMS()
                    return
                }
            }
        }
        return
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


        // Checks if file exists, if not creates the file
        when {
            (!jsonFile.exists()) -> jsonFile.createNewFile()
            (!smsFile.exists()) -> smsFile.createNewFile()
        }
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

            requestRole()
            requestPerms(permissions)

            checkFiles()


            dialog.dismiss()
        }
    }

    private fun loadSMS() {
        var smsMessages: HashMap<String, ArrayList<SMSBaseObject>> = HashMap<String, ArrayList<SMSBaseObject>>()
        val smsFile = File(smsDirectory)

        val contentResolver = this.contentResolver
        val cursor: Cursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,
                                            arrayOf(Telephony.Sms.Inbox.ADDRESS, Telephony.Sms.Inbox.BODY, Telephony.Sms.Inbox.DATE_SENT),
                                            null,
                                            null,
                                            Telephony.Sms.ADDRESS) as Cursor
        var totalSms = cursor.count


        if (cursor.moveToFirst()) {
            for (i in 0 until totalSms) {

                var smsBaseObject = SMSBaseObject(cursor.getString(1), cursor.getLong(2))

                // Checks if there are existing messages from the same address
                if (smsMessages.containsKey(cursor.getString(0))) {

                    smsMessages.get(cursor.getString(0))?.add(smsBaseObject)

                } else {

                    var smsBaseArray = ArrayList<SMSBaseObject>()
                    smsBaseArray.add(smsBaseObject)

                    smsMessages.put(cursor.getString(0), smsBaseArray)
                }


                val writer = FileWriter(smsFile)
                Gson().toJson(smsMessages, writer)
                writer.close()

                cursor.moveToNext()
            }
        } else {
            throw RuntimeException("You have no new messages")
        }
        cursor.close()
    }




}





