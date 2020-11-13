package com.example.cachetube.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Video::class, CachedVideoFile::class], version = 4)
abstract class RoomDB: RoomDatabase() {

    abstract fun getVideoDao(): VideoDao

    abstract fun getCachedDao(): CachedFileDao


}