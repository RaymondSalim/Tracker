package com.reas.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d("LoginActivity", "onCreate: ")
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Checks if user is signed in
        if (currentUser != null) {
            loginSuccess()
        }

        registerButton.setOnClickListener {
            registerButton.isEnabled = false
            auth.createUserWithEmailAndPassword(textEmail.text.toString(), textPassword.text.toString())
                .addOnCompleteListener(this) {task ->
                    if (task.isSuccessful) {
                        Log.d("TAG", "onCreate: User created")
                        Toast.makeText(this, "User account created", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                    registerButton.isEnabled = true
                }.addOnFailureListener {
                    Log.e("LoginActivity", "onCreate: ", it )
                }
        }

        loginButton.setOnClickListener {
            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(textEmail.text.toString(), textPassword.text.toString())
                .addOnCompleteListener(this) {task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithEmail:success")
                        val user = auth.currentUser
                        loginSuccess()
                    } else {
                        loginButton.isEnabled = true
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loginSuccess() {
        val mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)

        val deviceInfo = HashMap<String, String>()
        deviceInfo["device"] = Build.DEVICE
        deviceInfo["manufacturer"] = Build.MANUFACTURER
        deviceInfo["model"] = Build.MODEL
        deviceInfo["fingerprint"] = Build.FINGERPRINT

        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/devices/${Build.DEVICE}")
        ref.setValue(deviceInfo).addOnSuccessListener {
            Log.d("LoginActivity", "Device Info Updated")
            Log.d("EEEE", "loginSuccess: ${deviceInfo}")
        }

        finish()
    }
}