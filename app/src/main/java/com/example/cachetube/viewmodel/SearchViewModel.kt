package com.example.cachetube.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cachetube.Repository
import com.example.cachetube.data.CachedVideoFile
import com.example.cachetube.data.Video
import kotlinx.coroutines.launch

class SearchViewModel
@ViewModelInject constructor(private val repository: Repository): ViewModel() {

    var listOfVideos: LiveData<List<Video>> = repository.allVideos()
//    var allCachedFiles: LiveData<List<CachedVideoFile>> = repository.getCachedFiles()

    fun insert(video: Video){
        viewModelScope.launch {
            repository.insert(video)
        }
    }

    fun deleteAll(){
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

//    fun addVideos(file: CachedVideoFile){
//        viewModelScope.launch {
//            repository.add(file)
//        }
//    }




}