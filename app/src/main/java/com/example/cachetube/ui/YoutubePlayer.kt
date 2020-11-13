package com.example.cachetube.ui

import android.os.Bundle
import android.os.PersistableBundle
import com.example.cachetube.databinding.YotubePlayerFragmentBinding
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer

class YoutubePlayer: YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    private lateinit var binding: YotubePlayerFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = YotubePlayerFragmentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.youtubePlayer.initialize("AIzaSyBpq08Qok2d55UJw_KydOZLRbnPyfTLjb4", this)

    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        youtubePlayer: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        youtubePlayer?.setPlayerStateChangeListener(object : YouTubePlayer.PlayerStateChangeListener{
            override fun onLoading() {
            }

            override fun onLoaded(p0: String?) {
            }

            override fun onAdStarted() {
            }

            override fun onVideoStarted() {
            }

            override fun onVideoEnded() {
            }

            override fun onError(p0: YouTubePlayer.ErrorReason?) {
            }

        })
        youtubePlayer?.setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener{
            override fun onPlaying() {
            }

            override fun onPaused() {
            }

            override fun onStopped() {
            }

            override fun onBuffering(p0: Boolean) {
            }

            override fun onSeekTo(p0: Int) {
            }

        })
        if(!wasRestored){
            youtubePlayer?.cueVideo(intent.getStringExtra("videoId2"))
        }
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
    }


}