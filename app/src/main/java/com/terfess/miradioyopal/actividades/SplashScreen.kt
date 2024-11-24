package com.terfess.miradioyopal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.terfess.miradioyopal.R
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.terfess.miradioyopal.recycler_view.DatoStation
import com.terfess.miradioyopal.recycler_view.DatosLocal
import com.terfess.miradioyopal.recycler_view.ObtenerRadios
import com.terfess.miradioyopal.servicios.BaseSql

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val intent = Intent(this, HomeScreen::class.java)

        val db = BaseSql(this)
        val versionLocal = db.obtenerVersionDb()

        val firebase = FirebaseFirestore.getInstance()

        val nombreDocumento = "1" //nombre del documento consultar

        firebase.collection("datos_funcionales").document(nombreDocumento)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val versionNube = document.getLong("version")

                    if (versionLocal < versionNube!!.toInt()) {
                        //descargar radios
                        db.onUpgrade(db.readableDatabase, versionLocal, versionNube.toInt())
                        ObtenerRadios().obtenerDatos(object : DatosLocal {
                            override fun guardar(datos: List<DatoStation>) {
                                for (dato in datos) {
                                    db.agregarRadio(dato)
                                }

                                startActivity(intent)
                                //save version data
                                db.agregarVersionDb(versionNube.toInt())
                            }
                        })
                    }

                } else {
                    println("El documento no existe")
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores de lectura
                println("Error firebase: $exception")
            }
    }
}