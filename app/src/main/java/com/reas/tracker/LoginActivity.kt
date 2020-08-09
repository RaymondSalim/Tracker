package com.reas.tracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlin.jvm.java as java

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Checks if user is signed in
        if (currentUser != null) {
            loginSuccess(currentUser)
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
                        loginSuccess(user!!)
                    } else {
                        loginButton.isEnabled = true
                        Log.w("TAG", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun loginSuccess(user: FirebaseUser) {
        val mainActivity = Intent(this, MainActivity::class.java)
        var bundle = Bundle()
        bundle.putString("email", user.email)
        bundle.putString("name", user.displayName)
        mainActivity.putExtra("bundle", bundle)
        startActivity(mainActivity)

        finish()
    }
}