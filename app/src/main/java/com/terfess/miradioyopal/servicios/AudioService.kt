package com.terfess.miradioyopal.servicios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class AudioService : MediaSessionService() {
//    private val customCommandClose = SessionCommand(ACTION_CLOSE, Bundle.EMPTY)
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    companion object {
        private const val CHANNEL_ID = "AUDIO_CHANNEL"
        private const val CHANNEL_NAME = "Canal de Audio"
        const val ACTION_CLOSE = "com.terfess.miradioyopal.ACTION_CLOSE"
    }

    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        //attributes for player, allow stop sound when this app lose the focus audio
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30000,  // Min buffer size
                60000,  // Max buffer size
                1500,   // Buffer para iniciar reproducción
                5000    // Buffer después de rebuffering
            )
            .build()

        //initialize exoplayer3
        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()


        //set created attributes on player
        player.setAudioAttributes(audioAttributes, true)

        //ser loadcontrol



//        val closeButton =
//            CommandButton.Builder()
//                .setDisplayName("Close Service")
//                .setIconResId(R.drawable.ic_x_close)
//                .setSessionCommand(customCommandClose)
//                .build()


        //initialize the media session
        mediaSession = MediaSession.Builder(this, player)
//            .setCallback(MyCallback())
//            .setCustomLayout(ImmutableList.of(closeButton))
            .build()



        //listener for errors
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                // Handle playback error, e.g., display an error message or retry logic
                println("AudioService :: Error playing radio stream: $error")
                // retrying and evite close noti
                player.playWhenReady = true

            }
        })

    }

//    private inner class MyCallback : MediaSession.Callback {
//
//        @UnstableApi
//        override fun onConnect(
//            session: MediaSession,
//            controller: MediaSession.ControllerInfo
//        ): MediaSession.ConnectionResult {
//            // Set available player and session commands.
//            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
//                .setAvailablePlayerCommands(
//                    MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
//                        .add(COMMAND_SEEK_TO_NEXT)
//                        .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
//                        .add(COMMAND_SEEK_TO_PREVIOUS)
//                        .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
//                        .build()
//                )
//                .setAvailableSessionCommands(
//                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
//                        .add(customCommandClose)
//                        .build()
//                )
//                .build()
//        }
//
//
//        override fun onCustomCommand(
//            session: MediaSession,
//            controller: MediaSession.ControllerInfo,
//            customCommand: SessionCommand,
//            args: Bundle
//        ): ListenableFuture<SessionResult> {
//            when (customCommand.customAction) {
//
//                ACTION_CLOSE -> {
//                    println("Stop Service Audio")
//                    onDestroy()
//                }
//            }
//            return Futures.immediateFuture(null)
//        }
//    }


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

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession!!.player
        if (player.playWhenReady) {
            // Make sure the service is not in foreground.
            player.pause()
        }
        stopSelf()
    }

    // allow others elements of the systeam entry at the session media
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession


    // release the player and media session in onDestroy
    override fun onDestroy() {
        super.onDestroy()
//        relaunchApp(this, SplashScreen::class.java)
        player.release()
        mediaSession?.release()
        stopSelf() // stop the service
        println("terminado")
    }

    private fun relaunchApp(context: Context, targetActivity: Class<*>) {
        val intent = Intent(context, targetActivity)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        println("relanzando")
    }

}