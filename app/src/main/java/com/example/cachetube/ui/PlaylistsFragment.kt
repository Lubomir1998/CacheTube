package com.example.cachetube.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cachetube.ApplicationClass
import com.example.cachetube.R
import com.example.cachetube.adapter.PlaylistsAdapter
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.PlaylistsFragmentBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PlaylistsFragment: Fragment(R.layout.playlists_fragment) {

    private lateinit var binding: PlaylistsFragmentBinding
    private val myApp = ApplicationClass()
    private lateinit var listener: PlaylistsAdapter.OnPlayListClicked

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PlaylistsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadList()

        listener = object : PlaylistsAdapter.OnPlayListClicked{
            override fun clickPlayList(playlist: Playlist) {

                val action = PlaylistsFragmentDirections.actionPlaylistsFragmentToSinglePlaylistFragment(playlist)
                view.findNavController().navigate(action)

            }

//            override fun addSongToPlaylist(playlist: Playlist) {}

        }


        binding.recyclerViewPlaylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PlaylistsAdapter(myApp.listOfPlayLists, listener)
            setHasFixedSize(true)
        }

        displayData(myApp.listOfPlayLists)

        binding.addPlaylist.setOnClickListener {
            view.findNavController().navigate(R.id.action_playlistsFragment_to_addPlaylistFragment)
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                loadList()
                val list = myApp.listOfPlayLists
                val playlist = PlaylistsAdapter(list, listener).getPlaylistAt(viewHolder.adapterPosition)
                list.remove(playlist)
                saveList()
            }


        }).attachToRecyclerView(binding.recyclerViewPlaylists)


    }

    private fun loadList(){
        val json = myApp.playlistSharedPrefs(requireContext()).getString("keyplay", "") ?: ""
        val type: Type = object : TypeToken<MutableList<Playlist?>?>() {}.type
        try {
            myApp.listOfPlayLists = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

    private fun displayData(list: MutableList<Playlist>){
        val adapter = binding.recyclerViewPlaylists.adapter as PlaylistsAdapter
        adapter.listOfPlaylists = list
        adapter.notifyDataSetChanged()
    }



    @SuppressLint("CommitPrefEdits")
    private fun saveList(){
        val editor = myApp.playlistSharedPrefs(requireContext()).edit()
        val json = Gson().toJson(myApp.listOfPlayLists)
        editor.putString("keyplay", json)
        editor.apply()
    }

}