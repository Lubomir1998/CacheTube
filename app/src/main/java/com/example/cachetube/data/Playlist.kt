package com.example.cachetube.data

import android.os.Parcel
import android.os.Parcelable
import java.io.File

data class Playlist(
    val name: String,
    var listOfVideosInThePlaylist: MutableList<File> = mutableListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }
}