package com.terfess.miradioyopal.servicios


import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.ServiceCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import com.terfess.miradioyopal.actividades.PantallaPrincipal
import com.terfess.miradioyopal.R

class ServicioAudio(): Service() {


    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "AUDIO_CHANNEL"
        private const val CHANNEL_NAME = "Canal de Audio"
        const val ACTION_PLAY_STOP = "com.terfess.miradioyopal.ACTION_PLAY"
        const val ACTION_ABRIR_PANTALLA = "com.terfess.miradioyopal.ACTION_ABRIR_PANTALLA"

        private lateinit var instancia : PantallaPrincipal
        fun declarador(objeto: PantallaPrincipal){
            instancia = objeto
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel() // Crear el canal de notificación
        iniciarServicio()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                // Opcional: Configurar otras propiedades del canal, como descripción, sonido, etc.
                // description = "Descripción del canal de notificación"
                // setSound(soundUri, audioAttributes)
            }

            // Registrar el canal con el NotificationManager
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun iniciarServicio() {
        println("En iniciar servicio")
        val playStopAccion = getButtonPendingIntent(ACTION_PLAY_STOP) //accion para comenzar reproduccion

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle("Estas escuchando: ${PantallaPrincipal.nombreRadioNoti}")
            .setContentText("- Presiona para abrir la app")
            .setContentIntent(volverAPantalla())
            .addAction(R.drawable.ic_stop, "Detener/Reanudar", playStopAccion)
            .setSmallIcon(R.drawable.ic_noti)
            // Agregar cualquier otra configuración necesaria para la notificación
            .build()


        // Iniciar el servicio en primer plano con la notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        }else{
            // Iniciar servicio en primer plano en versiones anteriores a Android Q
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            startForeground(NOTIFICATION_ID, notification)
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun playStop() {
        //detener o reproducir audio segun estado del reproductor
        instancia.manejarReproductor()
    }

    private fun getButtonPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, ServicioAudio::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    private fun volverAPantalla(): PendingIntent? {
        val intent = Intent(this, PantallaPrincipal::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_PLAY_STOP -> {
                //detener o reanudar el reproductor
                playStop()
            }

            ACTION_ABRIR_PANTALLA -> {
                //abrir la app desde la notidicacion en la misma pantalla
                val intent = Intent(this, PantallaPrincipal::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

}

