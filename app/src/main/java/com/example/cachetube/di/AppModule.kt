package com.example.cachetube.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.cachetube.data.RoomDB
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.lang.reflect.Type
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDB(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        RoomDB::class.java,
        "room_db"
    )
        .fallbackToDestructiveMigration()
        .build()


    @Singleton
    @Provides
    fun provideDao(db: RoomDB) = db.getVideoDao()

    @Singleton
    @Provides
    fun provideCachedDao(db: RoomDB) = db.getCachedDao()

    @Singleton
    @Provides
    fun getSharedPrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("sharedPrefs", MODE_PRIVATE)




}