@file:Suppress("DEPRECATION")

package com.example.i_reached.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.i_reached.R
import com.example.i_reached.activity.MainActivity
import com.example.i_reached.databinding.FragmentMapBinding
import com.example.i_reached.helper.SQLHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private lateinit var binding: FragmentMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapCircle: Circle
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(context, "Grant permission to access location.", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true

        getCurrentLocation(SQLHelper(requireContext()))

        googleMap.setOnMapClickListener {
            googleMap.addMarker(MarkerOptions().position(it))
            mapCircle = googleMap.addCircle(
                CircleOptions().center(it)
                    .radius(500.0)
                    .fillColor(R.color.green)
                    .strokeWidth(4f)
                    .strokeColor(Color.GREEN)
            )
            googleMap.addMarker(MarkerOptions().position(it))
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(it))
            binding.tvCoordinatesLat.text = it.latitude.toFloat().toString()

            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? =
                geocoder.getFromLocation(it.latitude, it.longitude, 1)
            binding.tvLocation.text = addresses!![0].getAddressLine(0)

            binding.tvCoordinatesLong.text = it.longitude.toFloat().toString()
            binding.llSave.visibility = View.VISIBLE
        }

    }

    private fun getCurrentLocation(db: SQLHelper) {

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location: Location = task.result
            val myLocation = LatLng(location.latitude, location.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))

            checkIfInCircle(db, location)

        }
    }

    private fun checkIfInCircle(db: SQLHelper, location: Location) {
        val data = db.dataGetter
        if (data.count != 0) {
            while (data.moveToNext()) {
                val distance = FloatArray(data.count)
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    data.getString(4).toDouble(),
                    data.getString(5).toDouble(),
                    distance
                )
                if (distance[0] <= data.getString(2).toFloat() && data.getString(3) == "true") {
                        notifyUser(data)
//                        val mediaPlayer = MediaPlayer.create((activity as MainActivity).applicationContext, R.raw.sound)
//                        mediaPlayer.start()
                        break
                }
            }
        }
    }

    private fun notifyUser(data: Cursor) {
        if(isAdded && context != null) {
            val intent = Intent(requireContext(), MainActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            intent.putExtra("notify", "notification")
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val pendingIntent =
                getActivity(context, 0, intent, 0)
            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(requireContext(), "channel")
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            builder.setContentTitle("Approaching Location...")
                .setContentText("You are approaching your location of alert ${data.getString(1)}.")
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel =
                NotificationChannel("channel_id", "nothing", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId("channel_id")
            notificationManager.notify(0, builder.build())
        } else {
            Log.i("Error", "No fragment attached to context.")
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            100
        )
    }

    private fun searchLocation() {

        val locationText = binding.search.text.toString()
        var list: List<Address>? = null

        if (TextUtils.isEmpty(locationText) || locationText == "") {
            Toast.makeText(context, "Enter location", Toast.LENGTH_SHORT).show()
        } else {
            val geoCoder = Geocoder(requireContext())
            try {
                list = geoCoder.getFromLocationName(locationText, 1)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error : ${e.message}", Toast.LENGTH_SHORT).show()
            }
            if (list!!.isEmpty()) {
                Toast.makeText(context, "No results found.", Toast.LENGTH_SHORT).show()
            } else {
                binding.llSave.visibility = View.VISIBLE
                val address = list[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mapCircle = googleMap.addCircle(
                    CircleOptions().center(latLng)
                        .radius(500.0)
                        .fillColor(R.color.green)
                        .strokeWidth(4f)
                        .strokeColor(Color.GREEN)
                )
                googleMap.addMarker(MarkerOptions().position(latLng).title(locationText))
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                binding.tvCoordinatesLat.text = address.latitude.toFloat().toString()
                binding.tvCoordinatesLong.text = address.longitude.toFloat().toString()
                binding.tvLocation.text = address.getAddressLine(0)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val DB = SQLHelper(requireContext())

        requestPermission()

        binding.ivSearch.setOnClickListener {
            searchLocation()
        }

        binding.btnSave.setOnClickListener {
            if (TextUtils.isEmpty(binding.etTitle.text)) {
                Toast.makeText(context, "Add some title too.", Toast.LENGTH_SHORT).show()
            } else {

                DB.addData(
                    binding.etTitle.text.toString().trim(),
                    ((binding.seekbar.progress * 500) + 500).toString(),
                    "true",
                    binding.tvCoordinatesLat.text.toString().trim(),
                    binding.tvCoordinatesLong.text.toString().trim()
                )
                Toast.makeText(context, "Location Alert Added", Toast.LENGTH_SHORT).show()

                mapCircle.radius = 0.0
                binding.llSave.visibility = View.GONE
                binding.search.text.clear()
            }
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
                mapCircle.radius = ((binding.seekbar.progress * 500) + 500).toDouble()
                "${mapCircle.radius} m".also { binding.tvRadius.text = it }
            }
        })

        binding.map.onCreate(savedInstanceState)
        binding.map.onPause()
        binding.map.onResume()
        try {
            MapsInitializer.initialize(requireContext())
        } catch (e: Exception) {
            e.stackTrace
        }
        binding.map.getMapAsync(this)

    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(p0: Location) {}

}