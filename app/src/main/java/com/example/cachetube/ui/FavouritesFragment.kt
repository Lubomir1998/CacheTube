package com.example.cachetube.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cachetube.ApplicationClass
import com.example.cachetube.MusicService
import com.example.cachetube.R
import com.example.cachetube.adapter.CachedVideoAdapter
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.FavouritesFragmentBinding
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.lang.Exception
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class FavouritesFragment: Fragment(R.layout.favourites_fragment) {

    @Inject lateinit var sharedPrefs: SharedPreferences
    private lateinit var binding: FavouritesFragmentBinding
    private lateinit var listener: CachedVideoAdapter.OnCachedVideoClickedListener
    private val myApp = ApplicationClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FavouritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadList()

        listener = object : CachedVideoAdapter.OnCachedVideoClickedListener{

            override fun onVideoClicked(file: File, position: Int) {
                val intent = Intent(requireContext(), MusicService::class.java)
                Util.startForegroundService(requireContext(), intent)
                val action = FavouritesFragmentDirections.actionFavouritesFragmentToNowPlayingFragment2(true, position, true, false, Playlist("" , mutableListOf()))
                view.findNavController().navigate(action)
            }


        }


        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CachedVideoAdapter(myApp.favouritesVideoList.toTypedArray(), listener, requireContext())
            setHasFixedSize(true)
        }
    }

    private fun loadList(){
        val json = sharedPrefs.getString("listKey", "") ?: ""
        val type: Type = object : TypeToken<MutableList<File?>?>() {}.type
        try {
            myApp.favouritesVideoList = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

}