package com.example.cachetube.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class CachedFileDao: BaseDao<CachedVideoFile> {


    @Update
    abstract suspend fun updateFile(file: CachedVideoFile)

    @Query("SELECT * FROM CachedVideoFile")
    abstract fun getAllCachedFiles(): LiveData<List<CachedVideoFile>>

    @Query("SELECT * FROM CachedVideoFile WHERE isFavourite = 1")
    abstract fun getFavouritesCachedFiles(): LiveData<List<CachedVideoFile>>
}