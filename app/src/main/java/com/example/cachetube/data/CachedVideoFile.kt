package com.example.cachetube.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CachedVideoFile (
    @PrimaryKey
    var name: String = "",
    var imgUrl: String = "",
    var isFavourite: Boolean = false
)