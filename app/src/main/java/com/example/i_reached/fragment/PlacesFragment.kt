package com.example.i_reached.fragment

import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.i_reached.R
import com.example.i_reached.helper.SQLHelper
import com.example.i_reached.adapter.AlertAdapter
import com.example.i_reached.model.Alert
import java.util.ArrayList

class PlacesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var alertAdapter: AlertAdapter
    private lateinit var alertList: ArrayList<Alert>
    private lateinit var DB : SQLHelper
    private lateinit var data : Cursor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout =  inflater.inflate(R.layout.fragment_places, container, false)

        recyclerView = layout.findViewById(R.id.recyclerViewPlaces)
        alertList = ArrayList()
        alertAdapter = AlertAdapter(requireContext(), alertList)
        recyclerView.adapter = alertAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        DB = SQLHelper(requireContext())
        data = DB.dataGetter

        showData(data, alertList)

        return layout
    }

    private fun showData(data: Cursor, alertList: ArrayList<Alert>) {
        if (data.count == 0) {
            Toast.makeText(context, "There is no item in alerts.", Toast.LENGTH_SHORT).show()
        }
        while (data.moveToNext()) {
            alertList.add(Alert(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5)))
        }
    }
}