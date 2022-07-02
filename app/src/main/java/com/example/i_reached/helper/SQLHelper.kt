package com.example.i_reached.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLHelper(context: Context) : SQLiteOpenHelper(context, DB_Name, null, 1) {

    companion object {
        const val DB_Name = "alerts.db "
        const val TB_Name = "Alert "
        const val id = "ID"
        const val title = "A_title"
        const val radius = "A_radius"
        const val isChecked = "A_checked"
        const val latitude = "A_lat"
        const val longitude = "A_long"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL("create table $TB_Name(ID INTEGER PRIMARY KEY AUTOINCREMENT, A_title TEXT, A_radius TEXT, A_checked TEXT, A_lat TEXT, A_long TEXT)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("DROP TABLE IF EXISTS $TB_Name")
    }

    fun addData(
        title_text: String,
        radius_text: String,
        isCheckedText: String,
        latText: String,
        lonText: String
    ) {
        val DB = this.writableDatabase
        val values = ContentValues()
        values.put(title, title_text)
        values.put(radius, radius_text)
        values.put(isChecked, isCheckedText)
        values.put(latitude, latText)
        values.put(longitude, lonText)

        DB.insert(TB_Name, null, values)
    }

    fun deleteData(id: String): Int {
        val DB = this.writableDatabase
        return DB.delete(TB_Name, "id = ?", arrayOf(id))
    }

    val dataGetter: Cursor
        get() {
            val DB = this.writableDatabase
            return DB.rawQuery("select * from $TB_Name", null)
        }

}