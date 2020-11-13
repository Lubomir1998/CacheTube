package com.example.cachetube.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.PlaylistItemBinding

class AddSongToPlaylistFromPlayerAdapter(var listPlayLists: MutableList<Playlist>, var listener: OnSongFromPlayerToPlaylist): RecyclerView.Adapter<AddSongToPlaylistFromPlayerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentPlaylist = listPlayLists[position]

        holder.playlistTextView.text = currentPlaylist.name
        holder.playlistSizeTextView.text = currentPlaylist.listOfVideosInThePlaylist.size.toString()

        holder.addToPlaylist(listener, currentPlaylist)
    }

    override fun getItemCount(): Int = listPlayLists.size


    class MyViewHolder(itemView: PlaylistItemBinding): RecyclerView.ViewHolder(itemView.root){
        val playlistTextView = itemView.playlistTextView
        val playlistSizeTextView = itemView.listSize

        fun addToPlaylist(listener: OnSongFromPlayerToPlaylist, playlist: Playlist){
            itemView.setOnClickListener {
                listener.addSongToPlaylist(playlist)
            }
        }
    }


    interface OnSongFromPlayerToPlaylist{
        fun addSongToPlaylist(playlist: Playlist)
    }

}