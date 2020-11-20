package com.example.cachetube.ui

import android.annotation.SuppressLint
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cachetube.ApplicationClass
import com.example.cachetube.MusicService
import com.example.cachetube.R
import com.example.cachetube.adapter.AddSongToPlaylistFromPlayerAdapter
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.AddSongToPlaylistFromPlayerBinding
import com.example.cachetube.databinding.MyPlayerFragmentBinding
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
import com.google.android.exoplayer2.util.Util.startForegroundService
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class MyPlayerFragment: Fragment(R.layout.my_player_fragment) {

    @Inject lateinit var sharedPrefs: SharedPreferences
    private lateinit var binding: MyPlayerFragmentBinding
    private lateinit var mBinding: AddSongToPlaylistFromPlayerBinding
    private var mService: MusicService? = null
    private var mBound: Boolean = false
    private lateinit var listener: AddSongToPlaylistFromPlayerAdapter.OnSongFromPlayerToPlaylist
    private val args: MyPlayerFragmentArgs by navArgs()
    private val myApp = ApplicationClass()
    private var isFullScreen = false
    private var isRepeating = false

    private val TAG = "MyPlayerFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MyPlayerFragmentBinding.inflate(inflater, container, false)
        mBinding = AddSongToPlaylistFromPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }


    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.LocalBinder
            mService = binder.getService()
            binding.videoView.player = mService!!.player
            mBound = true
            checkIfVideoIsFavourite()
            repeatingMode()

            mService!!.player!!.addListener(object : Player.EventListener {
                override fun onTracksChanged(
                    trackGroups: TrackGroupArray,
                    trackSelections: TrackSelectionArray
                ) {
                    super.onTracksChanged(trackGroups, trackSelections)
                    checkIfVideoIsFavourite()
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPlayer()
        fullScreenMode()
        loadList()
        loadPlaylistList()
        loadRepeatingState()

        // add video to playlist
        binding.videoView.findViewById<ImageView>(R.id.addToPlaylistBtn).setOnClickListener {
            if(mService?.videosList != null){
                val song: File = mService?.videosList!![mService?.player!!.currentWindowIndex]

                val dialog = AlertDialog.Builder(requireActivity(), android.R.style.Theme_Light_NoTitleBar_Fullscreen).create()
                dialog.setView(mBinding.root)

                mBinding.backBtn.setOnClickListener {
                    dialog.dismiss()
                    val parentViewGroup = mBinding.root.parent as ViewGroup
                    parentViewGroup.removeAllViews()
                }

                listener = object : AddSongToPlaylistFromPlayerAdapter.OnSongFromPlayerToPlaylist{

                    override fun addSongToPlaylist(playlist: Playlist) {
                        for(singlePlaylist in myApp.listOfPlayLists){
                            if(singlePlaylist.name == playlist.name){
                                if(singlePlaylist.listOfVideosInThePlaylist.contains(song)){
                                    Snackbar.make(requireView(), "The video is already in the playlist", Snackbar.LENGTH_SHORT).show()

                                    dialog.dismiss()
                                    val parentViewGroup = mBinding.root.parent as ViewGroup
                                    parentViewGroup.removeAllViews()
                                    return
                                }
                                else {
                                    val videoListInThePlaylist =
                                        singlePlaylist.listOfVideosInThePlaylist
                                    videoListInThePlaylist.add(song)
                                    savePlaylist()

                                    dialog.dismiss()
                                    val parentViewGroup = mBinding.root.parent as ViewGroup
                                    parentViewGroup.removeAllViews()
                                    return
                                }
                            }
                        }

                    }
                }

                mBinding.recyclerview01.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = AddSongToPlaylistFromPlayerAdapter(myApp.listOfPlayLists, listener)
                    setHasFixedSize(true)
                }

                dialog.show()
            }
        }

        // set video repeating
        binding.videoView.findViewById<ImageView>(R.id.repeatBtn).setOnClickListener {
            isRepeating = !isRepeating
            repeatingMode()
            saveRepeatingState()
        }

        // favourite image button click
        binding.videoView.findViewById<ImageView>(R.id.favouritesImageButton).setOnClickListener {
            if(mService?.videosList != null) {
                val videoFile = mService?.videosList!![mService?.player!!.currentWindowIndex]

                if (myApp.favouritesVideoList.contains(videoFile)) {
                    myApp.favouritesVideoList.remove(videoFile)
                    binding.videoView.findViewById<ImageView>(R.id.favouritesImageButton)
                        .setImageResource(R.drawable.not_favourite_img)
                }
                else {
                    myApp.favouritesVideoList.add(videoFile)
                    binding.videoView.findViewById<ImageView>(R.id.favouritesImageButton)
                        .setImageResource(R.drawable.favourites_img)
                }
                saveList()
            }
        }


        // full screen image button click
        binding.videoView.findViewById<ImageView>(R.id.exoplayer_fullscreen_icon).setOnClickListener {
            isFullScreen = !isFullScreen
            fullScreenMode()
        }


    }


    private fun checkIfVideoIsFavourite(){
        if(mService?.videosList != null) {
            val videoFile = mService?.videosList!![mService?.player!!.currentWindowIndex]

            if (myApp.favouritesVideoList.contains(videoFile)) {
                binding.videoView.findViewById<ImageView>(R.id.favouritesImageButton)
                    .setImageResource(R.drawable.favourites_img)
            }
            else {
                binding.videoView.findViewById<ImageView>(R.id.favouritesImageButton)
                    .setImageResource(R.drawable.not_favourite_img)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun repeatingMode(){
        if(isRepeating){
            binding.videoView.findViewById<ImageView>(R.id.repeatBtn).setColorFilter(requireContext().resources.getColor(R.color.bluee))

            mService?.player!!.repeatMode = Player.REPEAT_MODE_ONE

        }
        else{
            binding.videoView.findViewById<ImageView>(R.id.repeatBtn).setColorFilter(requireContext().resources.getColor(R.color.white))
            mService?.player!!.repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    private fun fullScreenMode(){
        if(isFullScreen){
            binding.videoView.findViewById<ImageView>(R.id.exoplayer_fullscreen_icon).setImageResource(
                R.drawable.exit_full_screen_img
            )
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            binding.videoView.resizeMode = RESIZE_MODE_FILL
            val params = binding.videoView.layoutParams
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            binding.videoView.layoutParams = params
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavMenu
            )?.visibility = View.GONE
        }
        else{
            val params = binding.videoView.layoutParams
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            params.height = 0
            binding.videoView.layoutParams = params
            binding.videoView.resizeMode = RESIZE_MODE_FIT
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            binding.videoView.findViewById<ImageView>(R.id.exoplayer_fullscreen_icon).setImageResource(
                R.drawable.full_screen_img
            )
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavMenu
            )?.visibility = View.VISIBLE
        }
    }

    private fun startPlayer(){
        if(args.playerStarted && !args.isPlaylistStarted) {
            val intent = Intent(requireContext(), MusicService::class.java)
            intent.putExtra("position", args.position)
            intent.putExtra("fav", args.isFavouriteSelected)
            startForegroundService(requireContext(), intent)

            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        else if(args.isPlaylistStarted && args.playerStarted){
            val playlist = args.playlistSent

            val intent = Intent(requireContext(), MusicService::class.java)
            intent.putExtra("position", args.position)
            intent.putExtra("play_list_started", args.isPlaylistStarted)
            intent.putExtra("playlist_name", playlist!!.name)
            startForegroundService(requireContext(), intent)

            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        else{

            val intent = Intent(requireContext(), MusicService::class.java)
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)

        }
    }

    override fun onStop() {
        super.onStop()
        if(mBound) {
            requireActivity().unbindService(connection)
        }
        mBound = false
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveList(){
        val editor = sharedPrefs.edit()
        val json = Gson().toJson(myApp.favouritesVideoList)
        editor.putString("listKey", json)
        editor.apply()
    }

    private fun loadList(){
        val json = sharedPrefs.getString("listKey", "") ?: ""
        val type: Type = object : TypeToken<MutableList<File?>?>() {}.type
        try {
            myApp.favouritesVideoList = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

    private fun loadPlaylistList(){
        val json = myApp.playlistSharedPrefs(requireContext()).getString("keyplay", "") ?: ""
        val type: Type = object : TypeToken<MutableList<Playlist?>?>() {}.type
        try {
            myApp.listOfPlayLists = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

    private fun savePlaylist(){
//        val sharedPreferences = myApp.customPlaylistSharedPrefs(requireContext(), name)
//        val editor = sharedPreferences.edit()
//        val json = Gson().toJson(list)
//        editor.putString("${name}key", json)
//        editor.apply()

        val editor = myApp.playlistSharedPrefs(requireContext()).edit()
        val json = Gson().toJson(myApp.listOfPlayLists)
        editor.putString("keyplay", json)
        editor.apply()
    }

    private fun saveRepeatingState(){
        val sharedPreferences = requireContext().getSharedPreferences("repeat", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("same", isRepeating)
        editor.apply()
    }

    private fun loadRepeatingState(){
        val sharedPreferences = requireContext().getSharedPreferences("repeat", MODE_PRIVATE)
        isRepeating = sharedPreferences.getBoolean("same", false)
    }

}