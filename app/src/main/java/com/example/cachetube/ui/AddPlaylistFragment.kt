package com.example.cachetube.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.cachetube.ApplicationClass
import com.example.cachetube.R
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.AddPlaylistDialogBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import java.lang.reflect.Type

class AddPlaylistFragment: Fragment(R.layout.add_playlist_dialog) {

    private lateinit var binding: AddPlaylistDialogBinding
    private val myApp = ApplicationClass()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AddPlaylistDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadList()

        binding.editTextAddPlaylist.setOnEditorActionListener { _, actionId, _ ->
            val input = binding.editTextAddPlaylist.text.toString()
            if(actionId == EditorInfo.IME_ACTION_DONE){


                if (input.trim().isNotEmpty()) {
                    val playlist = Playlist(input)
                    myApp.listOfPlayLists.add(playlist)
                    saveList()
                    hideKeyboard(requireActivity())
                    view.findNavController()
                        .navigate(R.id.action_addPlaylistFragment_to_playlistsFragment)

                    true
                }
                else {
                    hideKeyboard(requireActivity())
                    Snackbar.make(requireView(), "Empty title", Snackbar.LENGTH_SHORT).show()
                    false
                }


            }
            else{
                false
            }
        }

        binding.returnBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_addPlaylistFragment_to_playlistsFragment)
        }

    }

    @SuppressLint("CommitPrefEdits")
    private fun saveList(){
        val editor = myApp.playlistSharedPrefs(requireContext()).edit()
        val json = Gson().toJson(myApp.listOfPlayLists)
        editor.putString("keyplay", json)
        editor.apply()
    }

    private fun loadList(){
        val json = myApp.playlistSharedPrefs(requireContext()).getString("keyplay", "") ?: ""
        val type: Type = object : TypeToken<MutableList<Playlist?>?>() {}.type
        try {
            myApp.listOfPlayLists = Gson().fromJson(json, type)
        }catch (e: Exception){}
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}