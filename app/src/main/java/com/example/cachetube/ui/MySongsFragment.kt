package com.example.cachetube.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cachetube.ApplicationClass
import com.example.cachetube.MusicService
import com.example.cachetube.R
import com.example.cachetube.adapter.CachedVideoAdapter
import com.example.cachetube.data.CachedVideoFile
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.MySongsFragmentBinding
import com.example.cachetube.viewmodel.MyVideosViewModel
import com.example.cachetube.viewmodel.SearchViewModel
import com.google.android.exoplayer2.util.Util.startForegroundService
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.lang.Exception
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class MySongsFragment: Fragment(R.layout.my_songs_fragment) {

    @Inject
    lateinit var sharedPrefs: SharedPreferences
    private lateinit var binding: MySongsFragmentBinding
    private lateinit var listener: CachedVideoAdapter.OnCachedVideoClickedListener
    private val model: MyVideosViewModel by viewModels()
    private var videosList: Array<out File>? = null
    private val myApp = ApplicationClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MySongsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = object : CachedVideoAdapter.OnCachedVideoClickedListener{

            override fun onVideoClicked(file: File, position: Int) {
                val intent = Intent(requireContext(), MusicService::class.java)
                startForegroundService(requireContext(), intent)
                val action = MySongsFragmentDirections.actionMySongsFragmentToNowPlayingFragment(true, position, false , false, Playlist("" , mutableListOf()))
                view.findNavController().navigate(action)
            }


        }


        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/YoutubeVideos"
        val directory = File(path)
        videosList = directory.listFiles()

//        initRecyclerView(videosList!!)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            if(videosList != null) {
                adapter = CachedVideoAdapter(videosList!!, listener, requireContext())
            }
            else{
                adapter = CachedVideoAdapter(arrayOf(), listener, requireContext())
            }
            setHasFixedSize(true)
        }


        loadList()

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val list = model.cachedVideosLiveData.value
                val file = CachedVideoAdapter(list!!, listener, requireContext()).getFileAt(viewHolder.adapterPosition)
                model.deleteSong(file, requireView())
                if(myApp.favouritesVideoList.contains(file)){
                    myApp.favouritesVideoList.remove(file)
                    saveList()
                }

                loadPlaylistList()
                for(playlist in myApp.listOfPlayLists){
                    val videosInPlaylist = playlist.listOfVideosInThePlaylist
                    if(videosInPlaylist.contains(file)){
                        videosInPlaylist.remove(file)
                        savePlaylist()
                    }
                }

            }



        }).attachToRecyclerView(binding.recyclerView)


    }






    private fun loadList(){
        val json = sharedPrefs.getString("listKey", "") ?: ""
        val type: Type = object : TypeToken<MutableList<File?>?>() {}.type
        try {
            myApp.favouritesVideoList = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

    @SuppressLint("CommitPrefEdits")
    private fun saveList(){
        val editor = sharedPrefs.edit()
        val json = Gson().toJson(myApp.favouritesVideoList)
        editor.putString("listKey", json)
        editor.apply()
    }

    private fun loadPlaylistList(){
        val json = myApp.playlistSharedPrefs(requireContext()).getString("keyplay", "") ?: ""
        val type: Type = object : TypeToken<MutableList<Playlist?>?>() {}.type
        try {
            myApp.listOfPlayLists = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

    private fun savePlaylist(){
        val editor = myApp.playlistSharedPrefs(requireContext()).edit()
        val json = Gson().toJson(myApp.listOfPlayLists)
        editor.putString("keyplay", json)
        editor.apply()
    }

}