package com.example.i_reached.adapter

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.i_reached.R
import com.example.i_reached.SQLHelper
import com.example.i_reached.model.Alert
import java.util.ArrayList

class AlertAdapter(private var context: Context, private var alertList: ArrayList<Alert>) :
    RecyclerView.Adapter<AlertAdapter.ViewHolder>() {

    private var DB = SQLHelper(context)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alertTitle: TextView = itemView.findViewById(R.id.alertTitle)
        val switch: SwitchCompat = itemView.findViewById(R.id.switch1)
        val alertLayout: RelativeLayout = itemView.findViewById(R.id.alertLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.place_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alert = alertList[position]
        holder.alertTitle.text = alert.title

        holder.switch.isChecked = alert.isChecked == "true"

        holder.alertLayout.setOnLongClickListener {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Alert delete Requested!!")
                .setMessage("You sure you want to delete the location alert?")
                .setPositiveButton("Okay") { _, _ ->
                    DB.deleteData(alert.id)
                    alertList.remove(alert)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Alert Deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { _, _ -> }
                .create()
                .show()
            return@setOnLongClickListener true
        }

        holder.switch.setOnCheckedChangeListener { _, _ ->

            if (holder.switch.isChecked) {
                val query = "UPDATE Alert  SET A_checked = true WHERE ID = ${alert.id}"
                DB.writableDatabase.execSQL(query)
                Toast.makeText(context, "Switch is turned on.", Toast.LENGTH_SHORT).show()
            } else {
                val query = "UPDATE Alert  SET A_checked = false WHERE ID = ${alert.id}"
                DB.writableDatabase.execSQL(query)
                Toast.makeText(context, "Switch is turned off.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return alertList.size
    }
}