package com.terfess.miradioyopal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import com.terfess.miradioyopal.R
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.terfess.miradioyopal.recycler_view.DatoEmisora
import com.terfess.miradioyopal.recycler_view.DatosLocal
import com.terfess.miradioyopal.recycler_view.ObtenerRadios
import com.terfess.miradioyopal.servicios.BaseSql

class SplashScreen : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val contexto = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //splash
        splashScreen.setKeepOnScreenCondition { true }

        val intent = Intent(this, PantallaPrincipal::class.java)

        val db = BaseSql(this)
        val versionLocal= db.obtenerVersionDb()

        val firebase = FirebaseFirestore.getInstance()

        val nombreDocumento = "1" //nombre del documento consultar

        firebase.collection("datos_funcionales").document(nombreDocumento)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val versionNube = document.getLong("version")

                    println("VersionNube: $versionNube")
                    if (versionLocal < versionNube!!.toInt()){
                        //descargar radios
                        db.onUpgrade(db.readableDatabase, versionLocal, versionNube.toInt())
                        ObtenerRadios().obtenerDatos(object : DatosLocal{
                            override fun guardar(datos: List<DatoEmisora>) {
                                for (dato in datos){
                                    db.agregarRadio(dato)
                                }

                                startActivity(intent)
                                Toast.makeText(contexto, "Completado", Toast.LENGTH_LONG).show()
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