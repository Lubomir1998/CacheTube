package com.example.cachetube.viewmodel

import android.os.Environment
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import java.io.File

class MyVideosViewModel: ViewModel() {


    val cachedVideosLiveData: MutableLiveData<Array<out File>> = MutableLiveData()


    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/YoutubeVideos"
    private val directory = File(path)
    private val videosList = directory.listFiles()

    init {
        cachedVideosLiveData.value = videosList
    }

    fun deleteSong(file: File, view: View){
        if(file.exists()){
            file.delete()
            Snackbar.make(view, "Video deleted", Snackbar.LENGTH_SHORT).show()
        }
    }

}