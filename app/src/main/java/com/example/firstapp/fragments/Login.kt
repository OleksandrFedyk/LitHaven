package com.example.firstapp.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.firstapp.R
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var sign_upButton: Button
    private lateinit var log_inButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sign_upButton = findViewById(R.id.sign_upButton)
        log_inButton = findViewById(R.id.log_inButton)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        auth = FirebaseAuth.getInstance()

        sign_upButton.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        log_inButton.setOnClickListener {
            val email: String = email.text.toString()
            val password: String = password.text.toString()
            if (email.isEmpty()) Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            if (password.isEmpty()) Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

        }



    }
}