package com.terfess.miradioyopal.actividades

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.terfess.miradioyopal.R
import com.terfess.miradioyopal.databinding.PantPrincipalBinding
import com.terfess.miradioyopal.recycler_view.AdapterHolderRadios
import com.terfess.miradioyopal.servicios.BaseSql
import com.terfess.miradioyopal.servicios.ServicioAudio

class PantallaPrincipal : AppCompatActivity() {
    private lateinit var binding: PantPrincipalBinding
    lateinit var reproductor: ExoPlayer
    private lateinit var mediaItem: MediaItem
    var nombreRadio = ""
    var url = ""

    companion object{
        var nombreRadioNoti = ""
        var urlNoti = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = PantPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        reproductor = ExoPlayer.Builder(this).build()

        //ads
        MobileAds.initialize(this) {}

        val mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        var enlaceFacebook = ""

        urlNoti = url
        nombreRadioNoti = nombreRadio

        val db = BaseSql(this)
        val dataList = db.obtenerListaRadios()
        println(dataList)

        //recycler radios
        val cajaRadios = binding.cajaRadios
        cajaRadios.layoutManager = GridLayoutManager(this, 2)
        cajaRadios.adapter = AdapterHolderRadios(this, dataList, this, binding.root)

        //si android mayor a 13 pedir permiso notificacion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pedirPermisoNotificacion()
        }
            reproductor.addListener(object : Player.Listener {
            override fun onIsLoadingChanged(isLoading: Boolean) {
                // Actualiza la visibilidad del estado de carga según el estado de carga actual del reproductor
                if (isLoading) {
                    // El reproductor está cargando
                    binding.play.visibility = View.GONE
                    binding.cargando.visibility = View.VISIBLE
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                // Actualiza la visibilidad del estado de carga cuando cambia el estado de reproducción del reproductor
                if (state == Player.STATE_READY && reproductor.isPlaying) {
                    // El reproductor está listo para reproducir pero aún no ha comenzado
                    binding.play.visibility = View.VISIBLE
                    binding.cargando.visibility = View.GONE
                }
            }
        })

    }

    fun iniciarReproductor(url: String) {

        val servicio = Intent(this, ServicioAudio::class.java)
        stopService(servicio)

        if (url == "") textoDireccional("Url no proporcionada")

        binding.play.setImageResource(R.drawable.ic_pausa)
        binding.infoReproduccion.visibility = View.VISIBLE

        mediaItem = MediaItem.fromUri(url)
        // Set the media item to be played.
        reproductor.setMediaItem(mediaItem)
        // Prepare the player.
        reproductor.prepare()

        reproductor.play()

        reproductor.playWhenReady = true

        if (reproductor.isLoading){
            // El reproductor está cargando
            binding.play.visibility = View.GONE
            binding.cargando.visibility = View.VISIBLE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(servicio)
            ServicioAudio.declarador(this)
        } else {
            startService(servicio)
            ServicioAudio.declarador(this)
        }

        binding.play.setOnClickListener {
            manejarReproductor()
        }
    }

    fun manejarReproductor() {
        if (reproductor.isPlaying) {
            //pausar reproductor
            reproductor.pause()
            binding.play.setImageResource(R.drawable.ic_play)
        } else if (reproductor.isLoading) {
            // Si el reproductor está cargando, no hacemos nada o podríamos mostrar un mensaje al usuario
            // indicando que la carga aún está en curso.
            // El reproductor está cargando
            binding.play.visibility = View.GONE
            binding.cargando.visibility = View.VISIBLE
        } else {
            // Start the playback.
            mediaItem = MediaItem.fromUri(url)
            // Set the media item to be played.
            reproductor.setMediaItem(mediaItem)
            // Prepare the player.
            reproductor.prepare()

            reproductor.play()

            binding.play.setImageResource(R.drawable.ic_pausa)
        }
    }


    fun textoDireccional(texto: String) {
        binding.textoHorizontal.text = texto
        binding.textoHorizontal.isSelected = true
    }

    fun abrirFacebook(urlPagina: String) {
        try {
            // Intent para abrir la aplicación de Facebook
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("fb://facewebmodal/f?href=$urlPagina")

            // Verificar si la aplicación de Facebook está instalada
            if (intent.resolveActivity(packageManager) != null) {
                // La aplicación de Facebook está instalada, abrir la página directamente
                startActivity(intent)
            } else {
                // La aplicación de Facebook no está instalada, abrir en el navegador web
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPagina))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun pedirPermisoNotificacion() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        //liberar - terminar recursos del reproductor
        reproductor.release()

        //detener el servicio de musica
        stopService(Intent(this, ServicioAudio::class.java))
    }

}