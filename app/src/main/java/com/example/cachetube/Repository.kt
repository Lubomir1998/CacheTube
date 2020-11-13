package com.example.cachetube

import com.example.cachetube.data.CachedFileDao
import com.example.cachetube.data.CachedVideoFile
import com.example.cachetube.data.Video
import com.example.cachetube.data.VideoDao
import javax.inject.Inject

class Repository
@Inject constructor(private val dao: VideoDao, private val dao2: CachedFileDao){

    suspend fun insert(video: Video) = dao.insert(video)

    suspend fun deleteAll() = dao.deleteAll()

    fun allVideos() = dao.getAll()

}