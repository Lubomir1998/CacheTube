package com.example.cachetube.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.PlaylistItemBinding
import java.io.File

class PlaylistsAdapter(var listOfPlaylists: MutableList<Playlist>, var listener: OnPlayListClicked): RecyclerView.Adapter<PlaylistsAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentPlaylist = listOfPlaylists[position]

        holder.playlistNameTextView.text = currentPlaylist.name
        holder.playlistSizeTextView.text = currentPlaylist.listOfVideosInThePlaylist.size.toString()

        holder.onPlaylistClicked(currentPlaylist, listener)
//        holder.addSongToPlaylist(currentPlaylist, listener)

    }

    override fun getItemCount(): Int = listOfPlaylists.size

    fun getPlaylistAt(position: Int): Playlist = listOfPlaylists[position]

    class MyViewHolder(itemView: PlaylistItemBinding): RecyclerView.ViewHolder(itemView.root){
        val playlistNameTextView = itemView.playlistTextView
        val playlistSizeTextView = itemView.listSize

        fun onPlaylistClicked(playlist: Playlist, listener: OnPlayListClicked){
            itemView.setOnClickListener {
                listener.clickPlayList(playlist)
            }
        }

//        fun addSongToPlaylist(playlist: Playlist, listener: OnPlayListClicked){
//            itemView.setOnClickListener {
//                listener.addSongToPlaylist(playlist)
//            }
//        }

    }

    interface OnPlayListClicked{
        fun clickPlayList(playlist: Playlist)
//        fun addSongToPlaylist(playlist: Playlist)
    }

}