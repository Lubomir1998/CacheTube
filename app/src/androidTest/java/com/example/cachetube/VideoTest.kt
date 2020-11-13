package com.example.cachetube

import android.os.Environment
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cachetube.data.RoomDB
import com.example.cachetube.data.Video
import com.example.cachetube.data.VideoDao
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class VideoTest {

    private lateinit var db: RoomDB
    private lateinit var dao: VideoDao

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setDb(){
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RoomDB::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.getVideoDao()
    }

    @After
    fun closeDb(){
        db.close()
    }


    @Test
    fun checkIfVideoIsCached() = runBlockingTest {
        val video = Video("a", "Eleni Foureira - Tómame - Official Music Video")
        val video2 = Video("s", "GONZALO HIGUAIN. NAPOLI CHANT")
        val video3 = Video("d", "FULL MATCH: Real Madrid 2 - 3 Barça (2017) Messi grabs dramatic late win in #ElClásico!!")
        val video4 = Video("f", "EVERY TOUCH: Thiago Alcantara& 39 s record-breaking Liverpool debut")

        dao.insert(video)
        dao.insert(video2)
        dao.insert(video3)
        dao.insert(video4)

        val listOfSearchedVideos = dao.getAll().getOrAwaitValue()
//        assertThat(listOfSearchedVideos.size).isEqualTo(4)


        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/YoutubeVideos"
        val directory = File(path)
        val listOfDownloadedFiles = directory.listFiles()

        val videoTitle = video.title.replace("[/#@%\$*+,;'●|=]".toRegex(), " ")

        dao.deleteAll()
        assertThat(listOfSearchedVideos.size).isEqualTo(0)

    }







}