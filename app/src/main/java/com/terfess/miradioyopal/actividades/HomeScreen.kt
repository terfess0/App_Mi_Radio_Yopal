package com.terfess.miradioyopal.actividades

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.terfess.miradioyopal.R
import com.terfess.miradioyopal.chat_radio.model.ChatMessage
import com.terfess.miradioyopal.chat_radio.recycler.ChatAdapter
import com.terfess.miradioyopal.databinding.ActivityHomeScreenBinding
import com.terfess.miradioyopal.databinding.BtnSheetChatBinding
import com.terfess.miradioyopal.recycler_view.AdapterHolderRadios
import com.terfess.miradioyopal.recycler_view.DatoStationLocal
import com.terfess.miradioyopal.servicios.AudioService
import com.terfess.miradioyopal.servicios.BaseSql
import com.terfess.miradioyopal.servicios.room.AppDatabase
import com.terfess.miradioyopal.servicios.room.model.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeScreen : AppCompatActivity() {
    lateinit var binding: ActivityHomeScreenBinding
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private lateinit var lyReproductor: LinearLayout
    private lateinit var btnPlay: ImageButton
    private lateinit var loadProgress: ProgressBar
    private lateinit var btnPower: FloatingActionButton
    private lateinit var dbFirebase: FirebaseFirestore

    val contextActivity = this

    object SharedData {
        val valueClose = MutableLiveData<Boolean>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        //ads
        loadAds()

        // Initialize views elements
        iniElementsBinding()

        // Firebase
        dbFirebase = FirebaseFirestore.getInstance()

        // Local database
        val db by lazy { AppDatabase.getDatabase(this) }

        // Recicler Stations
        val cajaRadios = binding.cajaRadios
        cajaRadios.layoutManager = GridLayoutManager(this, 2)

        var dataList: List<Station> = emptyList()
        CoroutineScope(Dispatchers.Default).launch {
            dataList = db.stationDao().getAllStation()
            cajaRadios.adapter =
                AdapterHolderRadios(this@HomeScreen, dataList, this@HomeScreen, binding.root)
        }

        // If android version upper than 13 request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pedirPermisoNotificacion()
        }

        SharedData.valueClose.observe(this@HomeScreen) {
            if (it) closeService()
        }

        btnPower.setOnClickListener {
            SharedData.valueClose.postValue(true)
            closeService()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                wantOut()
            }
        })
    }

    private fun iniElementsBinding() {
        lyReproductor = binding.infoReproduccion
        btnPlay = binding.play
        loadProgress = binding.cargando
        btnPower = binding.btnPower
    }

    private fun loadAds() {
        MobileAds.initialize(this) {}
        val mAdView = binding.adView

        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }


    fun starPlayer(radio: Station) {
        //set playlist radios on the player
        var lista: MutableList<MediaItem> = mutableListOf()
        CoroutineScope(Dispatchers.Default).launch {
            lista = getListMediaSources()
        }

        //hide errorAlert
        binding.errorAlert.visibility = View.GONE
        lyReproductor.visibility = View.VISIBLE

        // Start session token on Service
        val sessionToken =
            SessionToken(this, ComponentName(this, AudioService::class.java))

        controllerFuture =
            MediaController.Builder(this, sessionToken).buildAsync()


        controllerFuture.addListener({

            val mediaController = controllerFuture.get()

            mediaController.setMediaItems(lista)

            //start on selected radio
            mediaController.seekTo(radio.idStation - 1, 0)

            mediaController.prepare()
            mediaController.play()

            btnPlay.setOnClickListener {
                //hide errorAlert
                binding.errorAlert.visibility = View.GONE

                if (mediaController.isPlaying) {
                    mediaController.pause()
                } else {
                    mediaController.prepare()
                    mediaController.play()

                    loadProgress.visibility = View.VISIBLE
                    btnPlay.visibility = View.GONE

                    if (mediaController.isPlaying) {
                        loadProgress.visibility = View.GONE
                        btnPlay.visibility = View.VISIBLE
                    }
                }
            }

            lyReproductor.visibility = View.VISIBLE

            mediaController.addListener(object : Player.Listener {


                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (
                        mediaController.isPlaying
                        || mediaController.playWhenReady
                        || mediaController.isLoading
                    ) {
                        btnPower.visibility = View.VISIBLE
                    }


                    if (isPlaying) {
                        // Active playback.
                        btnPlay.setImageResource(R.drawable.ic_pausa)

                        //hide errorAlert
                        binding.errorAlert.visibility = View.GONE

                        if (!mediaController.isLoading) loadProgress.visibility = View.GONE

                        val mediaData = mediaController.currentMediaItem
                        textoDireccional(buildString {
                            append(getString(R.string.estas_escuchando))
                            append(mediaData?.mediaMetadata?.title)
                        })

                        println("Es live: ${mediaController.isCurrentMediaItemLive}")

                        lyReproductor.visibility = View.VISIBLE
                    } else {
                        btnPlay.setImageResource(R.drawable.ic_play)

                        mediaController.stop() //stop each time for at reanudar research the station
                        mediaController.prepare()

                        if (mediaController.playWhenReady) {
                            loadProgress.visibility = View.VISIBLE
                            btnPlay.visibility = View.GONE
                            textoDireccional(getString(R.string.buscando))
                        }
                    }
                }


                override fun onPlaybackStateChanged(state: Int) {
                    // Actualiza la visibilidad del estado de carga cuando cambia el estado de reproducción del reproductor
                    if (state == Player.STATE_READY && mediaController.isPlaying) {
                        // El reproductor está listo para reproducir pero aún no ha comenzado
                        btnPlay.visibility = View.VISIBLE
                        binding.cargando.visibility = View.GONE
                    }

                    if (state == Player.STATE_ENDED) {
                        // Verifica si es contenido en vivo y maneja la reproducción
                        val currentItem = mediaController.currentMediaItem
                        if (currentItem?.liveConfiguration != null) {
                            mediaController.seekToDefaultPosition()
                            mediaController.playWhenReady = true
                        }
                        println("Estate ended evitar next")
                    }

                }

                //on player error signal
                override fun onPlayerError(error: PlaybackException) {
                    // Imprime el error para depuración
                    println("AudioService :: Error playing radio stream: ${error.message}")

                    when (error.errorCode) {
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                            val errorMessage = getString(R.string.error_time_over)
                            radioError(errorMessage)
                            mediaController.pause()

                            val text = getString(R.string.baseTextDireccional)
                            textoDireccional(text)
                        }

                        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                            val media = mediaController.currentMediaItem
                            val nameRadio = media?.mediaMetadata?.title
                            val errorMessage = getString(R.string.error_source_station, nameRadio)
                            radioError(errorMessage)
                            mediaController.pause()

                            val text = getString(R.string.baseTextDireccional)
                            textoDireccional(text)
                        }

                        else -> {
                            val errorMessage = getString(R.string.error_base)
                            radioError(errorMessage)
                            mediaController.playWhenReady = true
                            mediaController.play()
                        }
                    }
                }

            })

            binding.chatOpcion.setOnClickListener {
                setRadioChat()
            }

            // MediaController is available here with controllerFuture.get()
        }, MoreExecutors.directExecutor())


    }

    private fun radioError(errorMessage: String) {
        val alert = binding.errorAlert

        alert.visibility = View.GONE
        alert.text = errorMessage
        alert.visibility = View.VISIBLE

        val errorBase = getString(R.string.error_base)
        if (errorMessage != errorBase) {
            binding.cargando.visibility = View.GONE
            btnPlay.visibility = View.VISIBLE
            btnPlay.setImageResource(R.drawable.ic_play)
        }
    }

    @OptIn(UnstableApi::class)
    private fun getListMediaSources(): MutableList<MediaItem> {
        //get radio stations from sql local
        val list = mutableListOf<MediaItem>()

        val roomDB by lazy { AppDatabase.getDatabase(this) }

        val radios = roomDB.stationDao().getAllStation()

        for (id in radios) {
            val station = MediaItem.Builder()
                .setUri(id.linkStream)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(id.name)
                        .setArtist(getString(R.string.app_mi_radio_yopal_noti))
                        .setArtworkUri(id.photo.toUri())
                        .setDurationMs(C.DATA_TYPE_MEDIA_PROGRESSIVE_LIVE.toLong())
                        .build()
                )
                .setLiveConfiguration(
                    MediaItem.LiveConfiguration.Builder()
                        .setTargetOffsetMs(C.TIME_UNSET)  // Usa el valor predeterminado de ExoPlayer
                        .setMinPlaybackSpeed(0.95f)      // Configura la velocidad mínima de reproducción
                        .setMaxPlaybackSpeed(1.05f)      // Configura la velocidad máxima de reproducción
                        .build()
                )
                .build()
            list.add(station)
        }


        return list
    }

    fun abrirFacebook(urlPage: String) {
        //verify link
        if (urlPage.isNotBlank() && urlPage.isNotEmpty()) {
            // Intent para abrir la aplicación de Facebook
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("fb://facewebmodal/f?href=$urlPage")

            // Verificar si la aplicación de Facebook está instalada
            if (intent.resolveActivity(packageManager) != null) {
                // La aplicación de Facebook está instalada, abrir la página directamente
                startActivity(intent)
            } else {
                // La aplicación de Facebook no está instalada, abrir en el navegador web
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPage))
                startActivity(browserIntent)
            }
        } else {
            val snk = Snackbar.make(
                binding.root,
                "No hay página de facebook disponible",
                Snackbar.LENGTH_LONG
            )
            snk.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
            snk.setBackgroundTint(ContextCompat.getColor(this, R.color.color_segundario))
            snk.show()
        }
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Snackbar.ANIMATION_MODE_SLIDE
            Snackbar.make(
                binding.root,
                "No podras recibir notificaciones",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun pedirPermisoNotificacion() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
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

    private var messages = mutableListOf<ChatMessage>()

    fun setRadioChat() {
        // Inflar el binding del BottomSheet
        val binding1chat = BtnSheetChatBinding.inflate(layoutInflater)

        val recyclerView = binding1chat.chatRecyclerView
        val messageInput = binding1chat.messageInput
        val sendButton = binding1chat.sendButton

        // Configurar RecyclerView y Adapter
        val chatAdapter = ChatAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatAdapter

        // Crear y configurar el BottomSheetDialog
        val btnSheetDialog = BottomSheetDialog(this, R.style.Theme_BottomSheetTheme)
        btnSheetDialog.setContentView(binding1chat.root)

        // Configurar el comportamiento del BottomSheet
        val bottomSheet =
            btnSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Mostrar el BottomSheetDialog
        btnSheetDialog.show()

        val roomId = "1piedemonte_radio_tv"

        // Enviar mensajes
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val chatMessage = ChatMessage(
                    user = "User1", // Cambia según el usuario actual
                    message = messageText,
                    timestamp = System.currentTimeMillis() // Agrega un timestamp
                )

                // Guardar el mensaje en Firestore
                dbFirebase.collection("radios").document(roomId).collection("messages")
                    .add(chatMessage)
                    .addOnSuccessListener {
                        messageInput.text.clear()
                        Log.d("ChatRadio", "Mensaje enviado correctamente")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatRadio", "Error al enviar el mensaje", e)
                    }
            }
        }

        // Escuchar cambios en la colección de mensajes
        dbFirebase.collection("radios").document(roomId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ChatRadio", "Listen failed.", e)
                    return@addSnapshotListener
                }

                // Actualizar mensajes
                if (snapshots != null) {
                    messages.clear() // Limpia la lista para evitar duplicados
                    for (doc in snapshots.documents) {
                        val message = doc.toObject(ChatMessage::class.java)
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }


    fun textoDireccional(texto: String) {
        binding.textoHorizontal.text = texto
        binding.textoHorizontal.isSelected = true
    }

    private fun closeService() {
        lyReproductor.visibility = View.GONE
        btnPower.visibility = View.GONE

        stopService(Intent(this, AudioService::class.java))
        controllerFuture.get().release()
    }

    override fun onDestroy() {
        super.onDestroy()

        //detener el servicio de musica
        stopService(Intent(this, AudioService::class.java))
    }

    private fun wantOut() {
        val builder = AlertDialog.Builder(this@HomeScreen)
        builder.setTitle("Salir")
        builder.setMessage("¿Seguro que quieres salir?")
            .setPositiveButton("Sí") { _, _ ->
                // Finish on confirm
                finishAffinity()
            }
        builder.setNegativeButton("No") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
    }

    fun getScreenPercentDp(
        context: Context,
        percent: Double
    ): Int {
        // Get screen height
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        // Calculate percent height
        return (screenHeight * percent).toInt()
    }
}