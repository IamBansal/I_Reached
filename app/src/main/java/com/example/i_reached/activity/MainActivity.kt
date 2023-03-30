package com.example.i_reached.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.i_reached.R
import com.example.i_reached.databinding.ActivityMainBinding
import com.example.i_reached.fragment.HelpFragment
import com.example.i_reached.fragment.MapFragment
import com.example.i_reached.fragment.PlacesFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.title = "I Reached?"

        binding.bottomm.setOnNavigationItemSelectedListener {
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
            binding.bottomm.selectedItemId = R.id.location
        } else {
            //For setting map fragment as default fragment.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapFragment()).commit()
        }

        binding.help.setOnClickListener {
            if (binding.help.tag!! == "open") {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MapFragment()).commit()
                binding.help.tag = "close"
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HelpFragment()).commit()
                binding.help.tag = "open"
            }
        }

    }
}

