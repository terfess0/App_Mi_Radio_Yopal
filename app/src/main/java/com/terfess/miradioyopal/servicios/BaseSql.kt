package com.terfess.miradioyopal.servicios

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.terfess.miradioyopal.recycler_view.DatoStation
import com.terfess.miradioyopal.recycler_view.DatoStationLocal

class BaseSql(context: Context) :
    SQLiteOpenHelper(context, "Local-Base-datos", null, 1) {


    override fun onCreate(db: SQLiteDatabase?) {
        // Create the table 'radios' in the sql db
        val createTableStr = (
                "CREATE TABLE radios (numRadio INTEGER PRIMARY KEY,  nombreRadio TEXT NOT NULL, linkStream TEXT NOT NULL, linkFacebook TEXT NOT NULL, foto TEXT NOT NULL);"
                )
        db?.execSQL(createTableStr)

        // Create table 'version' if not exists and initialize
        db?.execSQL("CREATE TABLE IF NOT EXISTS version (numVersion INTEGER NOT NULL)")
        db?.execSQL("INSERT OR IGNORE INTO version (numVersion) VALUES (1)")
    }




    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS radios")
        onCreate(db)
    }


    // Método para insertar datos en la base de datos
    fun agregarRadio(data: DatoStation) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("nombreRadio", data.name)
            put("linkStream", data.linkStream)
            put("linkFacebook", data.linkFacebook)
            put("foto", data.photo)
        }
        db.insert("radios", null, values)
        db.close()
       
    }


    // Método para obtener todos los datos de la base de datos
    fun obtenerListaRadios(): ArrayList<DatoStationLocal> {
        val dataList = ArrayList<DatoStationLocal>()
        val selectQuery = "SELECT * FROM radios"
        val db = this.readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        if (cursor.moveToFirst()) {
            do {
                val idRadio = cursor.getInt(0)
                val nombreRadio = cursor.getString(1)
                val linkStream = cursor.getString(2)
                val linkFacebook = cursor.getString(3)
                val foto = cursor.getString(4)

                // Crear un objeto DatoRadio con los datos obtenidos y agregarlo a la lista
                val datoRadio = DatoStationLocal(idRadio, nombreRadio, linkStream, linkFacebook, foto)
                dataList.add(datoRadio)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return dataList
    }

    fun obtenerVersionDb(): Int {
        val db = this.readableDatabase
        var version = 0

        val cursor = db.rawQuery("SELECT numVersion FROM version", null)

        if (cursor.moveToFirst()) {
            version = cursor.getInt(0)
        }
        cursor.close()
        db.close()

        return version
    }


    fun agregarVersionDb(newVersion: Int) {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("numVersion", newVersion)
        }

        // Utilizar INSERT OR REPLACE para garantizar un único valor de versión
        db.insertWithOnConflict("version", null, values, SQLiteDatabase.CONFLICT_REPLACE)

        db.close()
    }


}