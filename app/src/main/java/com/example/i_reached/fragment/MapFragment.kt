package com.example.i_reached.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import com.example.i_reached.R
import com.example.i_reached.SQLHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.lang.Exception

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var googleMap: GoogleMap
    private lateinit var search: EditText
    private lateinit var ivSearch: ImageView
    private lateinit var llSave : LinearLayout
    private lateinit var title : EditText
    private lateinit var seekBar: SeekBar
    private lateinit var coordinateTxt : TextView
    private lateinit var radiusText : TextView
    private lateinit var saveBtn : Button
    private var latSearch : Double? = null
    private var longSearch : Double? = null
    private lateinit var mapCircle : Circle
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

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

        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location: Location = task.result
            val myLocation = LatLng(location.latitude, location.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14f))
        }

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
            coordinateTxt.text =
                "Coordinates : (${it.latitude.toFloat()}, ${it.longitude.toFloat()})"
            llSave.visibility = View.VISIBLE
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

    private fun searchLocation(view: View) {

        val locationText = search.text.toString()
        var list: List<Address>? = null

        if (TextUtils.isEmpty(locationText) || locationText == "") {
            Toast.makeText(context, "Enter location", Toast.LENGTH_SHORT).show()
        } else {
            val geoCoder = Geocoder(this@MapFragment.context)
            try {
                list = geoCoder.getFromLocationName(locationText, 1)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "Error : ${e.message}", Toast.LENGTH_SHORT).show()
            }
            if(list!!.isEmpty()){
                Toast.makeText(context, "No results found.", Toast.LENGTH_SHORT).show()
            } else {
                llSave.visibility = View.VISIBLE
                val address = list[0]
                val latLng = LatLng(address.latitude, address.longitude)
                latSearch = address.latitude
                longSearch = address.longitude
                mapCircle = googleMap.addCircle(
                    CircleOptions().center(latLng)
                        .radius(500.0)
                        .fillColor(R.color.green)
                        .strokeWidth(4f)
                        .strokeColor(Color.GREEN)
                )
                googleMap.addMarker(MarkerOptions().position(latLng).title(locationText))
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                coordinateTxt.text =
                    "Coordinates : (${address.latitude.toFloat()}, ${address.longitude.toFloat()})"
            }
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val DB = SQLHelper(requireContext())

        requestPermission()

        val mapView = view.findViewById<MapView>(R.id.map)
        search = view.findViewById(R.id.search)
        ivSearch = view.findViewById(R.id.ivSearch)
        llSave = view.findViewById(R.id.llSave)
        title = view.findViewById(R.id.etTitle)
        radiusText = view.findViewById(R.id.tvRadius)
        seekBar = view.findViewById(R.id.seekbar)
        coordinateTxt = view.findViewById(R.id.tvCoordinates)
        saveBtn = view.findViewById(R.id.btnSave)

        ivSearch.setOnClickListener {
            searchLocation(view)
        }

        saveBtn.setOnClickListener {
            if (TextUtils.isEmpty(title.text)){
                Toast.makeText(context, "Add some title too.", Toast.LENGTH_SHORT).show()
            } else {

                DB.addData(title.text.toString().trim(),
                    ((seekBar.progress * 500) + 500).toString(),
                    "true",
                    latSearch.toString(),
                    longSearch.toString()
                )
                Toast.makeText(context, "Location Alert Added", Toast.LENGTH_SHORT).show()

                mapCircle.radius = 0.0
                llSave.visibility = View.GONE
                search.text.clear()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {
               mapCircle.radius = ((seekBar.progress * 500) + 500).toDouble()
                radiusText.text = mapCircle.radius.toString() + " m"
            }
        })

        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        try {
            MapsInitializer.initialize(requireContext())
        } catch (e: Exception) {
            e.stackTrace
        }
        mapView.getMapAsync(this)

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MapFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMyLocationButtonClick(): Boolean {
//        Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
//        Toast.makeText(context, "Current location: $p0\n", Toast.LENGTH_LONG).show()
    }

}