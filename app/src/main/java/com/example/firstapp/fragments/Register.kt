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
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var reg_sign_upButton: Button
    private lateinit var reg_email: EditText
    private lateinit var reg_password: EditText
    private lateinit var reg_AccountName: EditText
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

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
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        reg_sign_upButton = findViewById(R.id.reg_sign_upButton)
        reg_email = findViewById(R.id.reg_email)
        reg_password = findViewById(R.id.reg_password)
        reg_AccountName = findViewById(R.id.reg_AccountName)
        auth = FirebaseAuth.getInstance()

        reg_sign_upButton.setOnClickListener {

            val email: String = reg_email.text.toString()
            val password: String = reg_password.text.toString()
            val AccountName: String = reg_AccountName.text.toString()

            if (email.isEmpty()) Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            if (password.isEmpty()) Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            if (AccountName.isEmpty()) Toast.makeText(this, "Enter AccountName", Toast.LENGTH_SHORT).show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser

                        val userId = user?.uid
                        val userMap = hashMapOf(
                            "userId" to userId,
                            "email" to email,
                            "accountName" to AccountName
                        )

                        db.collection("users").document(userId!!)
                            .set(userMap)
                            .addOnSuccessListener {
                                Log.d(TAG, "User profile is saved in Firestore.")
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
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