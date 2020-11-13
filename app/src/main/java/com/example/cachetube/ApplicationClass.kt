package com.example.cachetube

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.cachetube.data.Playlist
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class ApplicationClass : Application(){

        var favouritesVideoList: MutableList<File> = mutableListOf()
        var listOfPlayLists: MutableList<Playlist> = mutableListOf()

        fun playlistSharedPrefs(context: Context): SharedPreferences =
                context.getSharedPreferences("playlistSharedPrefs", MODE_PRIVATE)


}