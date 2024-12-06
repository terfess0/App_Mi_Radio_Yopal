package com.terfess.miradioyopal.actividades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.terfess.miradioyopal.R
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.firestore.FirebaseFirestore
import com.terfess.miradioyopal.recycler_view.DatoStation
import com.terfess.miradioyopal.recycler_view.DatosLocal
import com.terfess.miradioyopal.recycler_view.ObtenerRadios
import com.terfess.miradioyopal.servicios.BaseSql
import com.terfess.miradioyopal.servicios.room.AppDatabase
import com.terfess.miradioyopal.servicios.room.model.Station
import com.terfess.miradioyopal.servicios.room.model.Version
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashScreen : AppCompatActivity() {
    private val firebase = FirebaseFirestore.getInstance()
    private val roomDB by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val intent = Intent(this, HomeScreen::class.java)

        val nombreDocumento = "1"



        firebase.collection("datos_funcionales").document(nombreDocumento)
            .get()
            .addOnSuccessListener { document ->
                CoroutineScope(Dispatchers.IO).launch {
                    val versionLocal = roomDB.versionDao().getVersion() ?: 0

                    if (document != null && document.exists()) {
                        val versionNube = document.getLong("version")
                        versionNube!!.toInt()
                        if (versionLocal < versionNube) {
                            //descargar radios
                            try {
                                roomDB.versionDao()
                                    .insertVersion(Version(numVersion = versionNube.toInt()))

                                roomDB.stationDao().deleteStationTable()

                                downloadStations(versionNube.toInt(), intent)
                            } catch (exp: Exception) {
                                println("error")
                            }
                        } else {
                            // Cambiar al hilo principal para iniciar la actividad si no hay descarga
                            withContext(Dispatchers.Main) {
                                startActivity(intent)
                                finish()
                            }
                        }

                    } else {
                        println("El documento no existe")
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores de lectura
                println("Error firebase: $exception")
            }

    }

    private fun downloadStations(newVersion: Int, intentToHome: Intent) {
        ObtenerRadios().obtenerDatos(object : DatosLocal {
            override fun guardar(datos: List<Station>) {
                CoroutineScope(Dispatchers.Default).launch {
                    for (dato in datos) {
                        println("Guardando... (${dato.name})")
                        roomDB.stationDao().insertStation(dato)
                    }

                    //save version data
                    roomDB.versionDao().updateVersion(Version(numVersion = newVersion))

                    startActivity(intentToHome)
                }
            }
        })
    }
}