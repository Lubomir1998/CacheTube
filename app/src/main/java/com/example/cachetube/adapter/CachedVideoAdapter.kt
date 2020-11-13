package com.example.cachetube.adapter

import android.R.attr.bitmap
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.cachetube.R
import com.example.cachetube.databinding.CachedFileItemBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream


class CachedVideoAdapter(
    var list: Array<out File>,
    var listener: OnCachedVideoClickedListener,
    val context: Context
): RecyclerView.Adapter<CachedVideoAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = CachedFileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentFile = list[position]

        val videoTitle = currentFile.name.substringBefore("https:")
        holder.songTitleTextView.text = videoTitle

        val a = currentFile.name.substringAfter("https:  i.ytimg.com vi ")
        val code = a.substring(0, 11)


        Picasso
            .with(context)
            .load("https://i.ytimg.com/vi/$code/mqdefault.jpg")
            .networkPolicy(NetworkPolicy.OFFLINE)
            .into(holder.imageView, object : Callback{
                override fun onSuccess() {}

                override fun onError() {
                    Picasso
                        .with(context)
                        .load("https://i.ytimg.com/vi/$code/mqdefault.jpg")
                        .into(holder.imageView, object : Callback{
                            override fun onSuccess() {}

                            override fun onError() {}
                        })
                }

            })

        holder.click(currentFile, listener, position)

    }

    override fun getItemCount(): Int = list.size

    fun getFileAt(position: Int): File{
        return list[position]
    }

    class MyViewHolder(itemView: CachedFileItemBinding): RecyclerView.ViewHolder(itemView.root){
        val songTitleTextView = itemView.fileName
        val imageView = itemView.fileImg


        fun click(file: File, listener: OnCachedVideoClickedListener, position: Int) {
            itemView.setOnClickListener {
                listener.onVideoClicked(file, position)
            }
        }



    }



    interface OnCachedVideoClickedListener{
        fun onVideoClicked(file: File, position: Int)
    }
}