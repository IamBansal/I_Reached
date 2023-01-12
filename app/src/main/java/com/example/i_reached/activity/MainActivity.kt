package com.example.i_reached.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.i_reached.R
import com.example.i_reached.fragment.HelpFragment
import com.example.i_reached.fragment.MapFragment
import com.example.i_reached.fragment.PlacesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var help: ImageView? = null
    private var bottomNavigationView: BottomNavigationView? = null
    private lateinit var toolbar: Toolbar

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "I Reached?"

        bottomNavigationView = findViewById(R.id.bottomm)
        bottomNavigationView?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.mapItem -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MapFragment()).commit()
                }
                R.id.location -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PlacesFragment()).commit()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        //For notification tap.
        val intent: Bundle? = intent.extras
        if (intent?.getString("notify") == "notification") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PlacesFragment()).commit()
            bottomNavigationView?.selectedItemId = R.id.location
        } else {
            //For setting map fragment as default fragment.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapFragment()).commit()
        }

        help = findViewById(R.id.help)
        help?.setOnClickListener {
            if (help?.tag!! == "open") {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MapFragment()).commit()
                help?.tag = "close"
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HelpFragment()).commit()
                help?.tag = "open"
            }
        }

    }
}

