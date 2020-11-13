package com.example.cachetube.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Video (
    @PrimaryKey
    var imgUrl: String = "",
    var title: String = "",
    var channel: String = "",
    var publishTime: String = "",
    var videoId: String? = null
)