package com.example.cachetube

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import androidx.core.net.toUri
import com.example.cachetube.data.Playlist
import com.example.cachetube.ui.MainActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.InputStream
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class MusicService: Service() {

    // Binder given to clients
    private val binder = LocalBinder()
    var player: SimpleExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    var videosList: Array<out File>? = null
    private val TAG = "MusicService"
    @Inject lateinit var sharedPrefs: SharedPreferences
    private val myApp = ApplicationClass()

    override fun onBind(intent: Intent?): IBinder? = binder


    @SuppressLint("ResourceType")
    override fun onCreate() {
        super.onCreate()

        val defaultTrackSelector = DefaultTrackSelector(this)
        defaultTrackSelector.setParameters(
            defaultTrackSelector.buildUponParameters().setMaxVideoSizeSd()
        )

        player = SimpleExoPlayer.Builder(this).setTrackSelector(defaultTrackSelector).build()

    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val position = intent?.getIntExtra("position", 0) ?: 0

        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "asd"))
        val concatenatingMediaSource = ConcatenatingMediaSource()


        loadList()

        // adding all cached videos to the playlist
        val isFavouritePlaying = intent?.getBooleanExtra("fav", false) ?: false
        val isPlaylistPlaying = intent?.getBooleanExtra("play_list_started", false) ?: false

        when {
            isFavouritePlaying -> {
                videosList = myApp.favouritesVideoList.toTypedArray()
                for(youTubeVideo in videosList!!){
                    val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                        youTubeVideo.toUri()
                    )
                    concatenatingMediaSource.addMediaSource(mediaSource)
                }
            }
            isPlaylistPlaying -> {
                val playlistName = intent?.getStringExtra("playlist_name")
                loadPlaylistList()

                for(cached in myApp.listOfPlayLists){
                    if(cached.name == playlistName){
                        videosList = cached.listOfVideosInThePlaylist.toTypedArray()
                    }
                }

                for (youTubeVideo in videosList!!) {
                    val mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                            youTubeVideo.toUri()
                        )
                    concatenatingMediaSource.addMediaSource(mediaSource)
                }

            }
            else -> {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/YoutubeVideos"
                val directory = File(path)
                videosList = directory.listFiles()

                for (youTubeVideo in videosList!!) {
                    val mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                            youTubeVideo.toUri()
                        )
                    concatenatingMediaSource.addMediaSource(mediaSource)
                }
            }
        }


        player!!.prepare(concatenatingMediaSource)
        player!!.seekTo(position, C.TIME_UNSET)
        player!!.playWhenReady = true
        player!!.repeatMode = Player.REPEAT_MODE_ALL


        // Displaying the notification
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,
            "channel_id",
            R.string.channelName,
            R.string.channelDesc,
            1,
            object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return videosList!![player.currentWindowIndex].name.substringBefore("https")
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent1 = Intent(this@MusicService, MainActivity::class.java)
                    intent1.action = "notification_tap"
                    return PendingIntent.getActivity(
                        this@MusicService,
                        0,
                        intent1,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    return null
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    try {
                        val input: InputStream?
                        val a = videosList!![player.currentWindowIndex].name.substringAfter("https:  i.ytimg.com vi ")
                        val code = a.substring(0, 11)
                        val url = URL("https://i.ytimg.com/vi/$code/mqdefault.jpg")

                        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                        connection.doInput = true
                        connection.connect()
                        input = connection.inputStream

                        return BitmapFactory.decodeStream(input)
                    }
                    catch (e: Exception) {
                        return null
                    }
                }

            },
            object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationStarted(
                    notificationId: Int,
                    notification: Notification
                ) {
                    startForeground(notificationId, notification)
                }

                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    //stopSelf()
                }
            }
        )


        playerNotificationManager?.setPlayer(player)

        return START_STICKY
    }




    override fun onDestroy() {
        super.onDestroy()
        if(playerNotificationManager != null) {
            playerNotificationManager!!.setPlayer(null)
        }
        player!!.release()
        player = null
    }


//    private fun isPlaying(): Boolean {
//        return player!!.playbackState == Player.STATE_READY && player!!.playWhenReady
//    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MusicService = this@MusicService
    }


    private fun loadList(){
        val json = sharedPrefs.getString("listKey", "") ?: ""
        val type: Type = object : TypeToken<MutableList<File?>?>() {}.type
        try {
            myApp.favouritesVideoList = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

    private fun loadPlaylistList(){
        val json = myApp.playlistSharedPrefs(this).getString("keyplay", "") ?: ""
        val type: Type = object : TypeToken<MutableList<Playlist?>?>() {}.type
        try {
            myApp.listOfPlayLists = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }


}