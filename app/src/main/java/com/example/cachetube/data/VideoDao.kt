package com.example.cachetube.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class VideoDao: BaseDao<Video> {


    @Query("SELECT * FROM video")
    abstract fun getAll(): LiveData<List<Video>>

    @Query("DELETE FROM video")
    abstract suspend fun deleteAll()


}