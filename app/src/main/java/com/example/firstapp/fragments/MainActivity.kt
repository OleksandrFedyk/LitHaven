package com.example.firstapp.fragments

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.firstapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private  var auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.drawer_nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.open,
            R.string.close
        )
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, android.R.color.white)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_history, R.id.nav_favBooks, R.id.nav_favAuthors, R.id.nav_logOut),
            drawerLayout
        )
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            loadUserData(uid)
        } else {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_history -> navController.navigate(R.id.libraryHistoryFragment)
                R.id.nav_favBooks -> navController.navigate(R.id.libraryLickedFragment)
                R.id.nav_favAuthors -> navController.navigate(R.id.favoriteAuthors)
                R.id.nav_logOut -> {
                    auth.signOut()
                    startActivity(Intent(this, Login::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(findNavController(R.id.fragmentContainerView), appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun loadUserData(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val accountName = document.getString("accountName") ?: "Not set"
                    val email = document.getString("email") ?: "Not set"

                    findViewById<TextView>(R.id.accName).text = accountName
                    findViewById<TextView>(R.id.accEmail).text = email
                } else {
                    Toast.makeText(this, "Document notet not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Loading error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        db.collection("authors").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val likes = document.getLong("likes") ?: 0

                    findViewById<TextView>(R.id.subCount).text = "$likes"
                } else {
                    Toast.makeText(this, "Your profile doesnt found in authors", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error to downoload likes: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


