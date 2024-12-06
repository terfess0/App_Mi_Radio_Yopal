package com.terfess.miradioyopal.recycler_view

import com.google.firebase.firestore.FirebaseFirestore
import com.terfess.miradioyopal.servicios.room.model.Station

interface DatosLocal {
    fun guardar(datos: List<Station>)
}

class ObtenerRadios {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun obtenerDatos(callback: DatosLocal) {
        val dataList = mutableListOf<Station>()

        db.collection("radios")
            //.orderBy("nombreRadio")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val idDoc: String = document.id
                    val nombre = document.getString("nombreRadio") ?: ""
                    val linkStream = document.getString("linkStream") ?: ""
                    val linkFacebook = document.getString("linkFacebook") ?: ""
                    val foto = document.getString("foto") ?: ""

                    val dato = Station(
                        idDocument = idDoc,
                        name = nombre,
                        linkStream = linkStream,
                        linkFacebook = linkFacebook,
                        photo = foto
                    )
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