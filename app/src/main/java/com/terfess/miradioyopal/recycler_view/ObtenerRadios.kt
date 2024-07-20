package com.terfess.miradioyopal.recycler_view

import com.google.firebase.firestore.FirebaseFirestore

interface DatosLocal{
    fun guardar(datos : List<DatoStation>)
}

class ObtenerRadios{
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun obtenerDatos(callback: DatosLocal) {
        val dataList = mutableListOf<DatoStation>()

        db.collection("radios")
            //.orderBy("nombreRadio")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombreRadio") ?: ""
                    val linkStream = document.getString("linkStream") ?: ""
                    val linkFacebook = document.getString("linkFacebook") ?: ""
                    val foto = document.getString("foto") ?: ""

                    val dato = DatoStation(nombre, linkStream, linkFacebook, foto)
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