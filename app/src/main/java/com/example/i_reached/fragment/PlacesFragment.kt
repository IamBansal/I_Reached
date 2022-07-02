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
import com.example.i_reached.SQLHelper
import com.example.i_reached.adapter.AlertAdapter
import com.example.i_reached.model.Alert
import java.util.ArrayList

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PlacesFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

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
            Toast.makeText(context, "There is no item in notes.", Toast.LENGTH_SHORT).show()
        }
        while (data.moveToNext()) {
            alertList.add(Alert(data.getString(0), data.getString(1), data.getString(2), data.getString(3), data.getString(4), data.getString(5)))
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlacesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlacesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}