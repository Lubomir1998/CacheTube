package com.example.cachetube.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cachetube.ApplicationClass
import com.example.cachetube.MusicService
import com.example.cachetube.R
import com.example.cachetube.adapter.CachedVideoAdapter
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.SinglePlaylistFragmentBinding
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.Exception
import java.lang.reflect.Type

class SinglePlaylistFragment: Fragment(R.layout.single_playlist_fragment) {

    private lateinit var binding: SinglePlaylistFragmentBinding
    private val myApp = ApplicationClass()
    private val args: SinglePlaylistFragmentArgs by navArgs()
    private lateinit var listener: CachedVideoAdapter.OnCachedVideoClickedListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SinglePlaylistFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playlistname.text = args.playlist.name

        binding.arrowBack.setOnClickListener {
            view.findNavController().navigate(R.id.action_singlePlaylistFragment_to_playlistsFragment)
        }

        // list of the videos in this playlist
        val list = args.playlist.listOfVideosInThePlaylist

        listener = object : CachedVideoAdapter.OnCachedVideoClickedListener{
            override fun onVideoClicked(file: File, position: Int) {
                val intent = Intent(requireContext(), MusicService::class.java)
                Util.startForegroundService(requireContext(), intent)
                val action = SinglePlaylistFragmentDirections
                    .actionSinglePlaylistFragmentToNowPlayingFragment(true, position, false, true, args.playlist)
                view.findNavController().navigate(action)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CachedVideoAdapter(list.toTypedArray(), listener, requireContext())
            setHasFixedSize(true)
        }


    }




}