package com.terfess.miradioyopal.recycler_view

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

interface DatosLocal{
    fun guardar(datos : List<DatoEmisora>)
}

class ObtenerRadios{
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun obtenerDatos(callback: DatosLocal) {
        val dataList = mutableListOf<DatoEmisora>()

        db.collection("radios")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombreRadio") ?: ""
                    val linkStream = document.getString("linkStream") ?: ""
                    val linkFacebook = document.getString("linkFacebook") ?: ""
                    val foto = document.getString("foto") ?: ""
                    println("HOLa")

                    val dato = DatoEmisora(nombre, linkStream, linkFacebook, foto)
                    dataList.add(dato)
                }
                callback.guardar(dataList)
            }
            .addOnFailureListener { exception ->
                // Manejar errores de lectura
                println("error firebase")
            }
    }
}