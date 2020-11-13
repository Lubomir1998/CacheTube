package com.example.cachetube.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cachetube.data.CachedVideoFile
import com.example.cachetube.data.Video
import com.example.cachetube.databinding.CachedVideoItemBinding
import com.example.cachetube.databinding.VideoItemBinding
import com.squareup.picasso.Picasso
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class VideoAdapter(
    private val context: Context,
    var listOfVideos: List<Video>,
    var listener: OnVideoClickedListener
) : RecyclerView.Adapter<VideoAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentVideo = listOfVideos[position]

        var videosList: Array<out File>? = null

        Picasso.with(context).load(currentVideo.imgUrl).into(holder.imageView)
        holder.channelAndTitleTextView.text = "${currentVideo.channel} - ${currentVideo.title}"

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(currentVideo.publishTime)

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val stringDate = formatter.format(date!!)

        holder.publishedTextView.text = "Published at $stringDate"

        holder.click(currentVideo, listener)
        holder.download(currentVideo, listener)

        //////////////////////////////////

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/YoutubeVideos"
        val directory = File(path)
        videosList = directory.listFiles()

        var isCached = false

        try {
            for (file in videosList!!) {
                val filename = file.name.substringBefore("https:").replace("[/#@%\$*+&,;'●|=]".toRegex(), " ")
                val videoTitle = currentVideo.title.substringBefore("https:").replace("[/#@%\$*+&,;'●|=]".toRegex(), " ")
                if (videoTitle == filename) {
                    isCached = true
                }
            }
        } catch (e: Exception) { }


        if (isCached) {
            holder.cachedTextView.visibility = View.VISIBLE
            holder.downLoadImg.isClickable = false
            holder.downLoadImg.visibility = View.INVISIBLE
        } else {
            holder.cachedTextView.visibility = View.GONE
            holder.downLoadImg.isClickable = true
            holder.downLoadImg.visibility = View.VISIBLE
        }


    }

    override fun getItemCount(): Int = listOfVideos.size


    class MyViewHolder(itemView: VideoItemBinding) : RecyclerView.ViewHolder(itemView.root) {
        val imageView = itemView.videoImg
        val channelAndTitleTextView = itemView.channelTitleTextView
        val publishedTextView = itemView.publishedTextView
        val cachedTextView = itemView.cachedTextView
        val downLoadImg = itemView.downloadBtn

        fun click(video: Video, listener: OnVideoClickedListener) {
            imageView.setOnClickListener {
                listener.onVideoClicked(video)
            }
        }

        fun download(video: Video, listener: OnVideoClickedListener) {
            downLoadImg.setOnClickListener {
                listener.onVideoDownloaded(video)
            }
        }

    }

    interface OnVideoClickedListener {
        fun onVideoClicked(video: Video)
        fun onVideoDownloaded(video: Video)
    }


}