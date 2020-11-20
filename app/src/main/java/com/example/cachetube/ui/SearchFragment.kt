package com.example.cachetube.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.example.cachetube.MusicService
import com.example.cachetube.R
import com.example.cachetube.adapter.VideoAdapter
import com.example.cachetube.data.Video
import com.example.cachetube.databinding.SearchFragmentBinding
import com.example.cachetube.viewmodel.SearchViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import javax.inject.Inject


@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.search_fragment) {

    private val TAG = "SearchFragment"
    private val API_KEY = "a"
    private lateinit var binding: SearchFragmentBinding
    private val model: SearchViewModel by viewModels()
    private lateinit var listener: VideoAdapter.OnVideoClickedListener

    var isNightMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shpr = requireActivity().getSharedPreferences("night", Context.MODE_PRIVATE)
        isNightMode = shpr.getBoolean("mode", false)

        nightMode(isNightMode)

        binding.nightModeBtn.setOnClickListener {
            isNightMode = !isNightMode
            sendIntent()
            nightMode(isNightMode)
            Log.d(TAG, "**********buttonClick: $isNightMode")
            val sharedPreferences = requireActivity().getSharedPreferences("night", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("mode", isNightMode).apply()
        }

        listener = object: VideoAdapter.OnVideoClickedListener{
            override fun onVideoClicked(video: Video) {
                val intent = Intent(requireContext(), YoutubePlayer::class.java)
                intent.putExtra("videoId2", video.videoId)
                startActivity(intent)
            }

            override fun onVideoDownloaded(video: Video) {
                downLoadVideoAndAudio(video.title, video.videoId!!, video.imgUrl)
            }
        }

        initRecyclerView()


        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchQuery()
                true
            }
            else{
                false
            }
        }



        model.listOfVideos.observe(requireActivity(), {
            displayData(it)

        })



        binding.swipeRefresh.setOnRefreshListener {
            searchQuery()
            binding.swipeRefresh.isRefreshing = false
        }



        // onViewCreated
    }

    private fun nightMode(isNightMode: Boolean){
        if(isNightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun sendIntent(){
        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

    private fun searchQuery(){
        val searchedWord = binding.searchEditText.text.toString()

        CoroutineScope(Dispatchers.Main).launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
            binding.errorTextView.visibility = View.INVISIBLE
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&key=$API_KEY&q=$searchedWord")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()


                try {
                    model.deleteAll()
                    val jsonObject = JSONObject(responseData!!)
                    val listOfVideos = jsonObject.getJSONArray("items")

                    for (i in 0 until listOfVideos.length()) {
                        var videoId: String

                        // sometimes when we search
                        // the top result is a channel instead of a video
                        // so videoId is missing and we don't get any results
                        try {
                            videoId = listOfVideos.getJSONObject(i).getJSONObject("id")
                                .getString("videoId")
                        } catch (e: JSONException) {
                            continue
                        }


                        val publishedAt = listOfVideos.getJSONObject(i).getJSONObject("snippet")
                            .getString("publishedAt")
                        val title = listOfVideos.getJSONObject(i).getJSONObject("snippet")
                            .getString("title")
                        val imgUrl = listOfVideos.getJSONObject(i).getJSONObject("snippet")
                            .getJSONObject("thumbnails")
                            .getJSONObject("medium")
                            .getString("url")

                        val channelName = listOfVideos.getJSONObject(i).getJSONObject("snippet")
                            .getString("channelTitle")


                        val youtubeVideo =
                            Video(imgUrl, title, channelName, publishedAt, videoId)


                        model.insert(youtubeVideo)

                    }


                } catch (e: Exception) {
                    Log.d(TAG, "onResponse: $e")
                }


                CoroutineScope(Dispatchers.Main).launch {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.recyclerView.visibility = View.VISIBLE
                    hideKeyboard(requireActivity())
                }

                // onResponse
            }

            override fun onFailure(call: Call, e: IOException) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        hideKeyboard(requireActivity())

                    binding.progressBar.visibility = View.INVISIBLE
                    binding.errorTextView.visibility = View.VISIBLE
                    Snackbar.make(requireView(), e.message.toString(), Snackbar.LENGTH_LONG)
                        .show()
                    }catch (e: Exception){ }
                }
            }
        })
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = VideoAdapter(requireContext(), mutableListOf(), listener)
            setHasFixedSize(true)
        }
    }

    private fun displayData(list: List<Video>){
        val adapter = binding.recyclerView.adapter as VideoAdapter
        adapter.listOfVideos = list
        adapter.notifyDataSetChanged()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }




    private fun downLoadVideoAndAudio(videoTitle: String, videoId: String, imgUrl: String){
        val youtubeLink = "https://www.youtube.com/watch?v=$videoId"
        val ytEx: YouTubeExtractor = @SuppressLint("StaticFieldLeak")
        object : YouTubeExtractor(requireContext()) {
            override fun onExtractionComplete(
                ytFiles: SparseArray<YtFile>?,
                videoMeta: VideoMeta?
            ) {
                val itag = 18
                //This is the download URL
                val downloadURL = ytFiles?.get(itag)?.url

                Log.d("TAG", "***********: $ytFiles")

                //now download it like a file
                RequestDownloadVideoStream().execute(
                    downloadURL,
                    videoTitle,
                    imgUrl
                )
            }
        }

        ytEx.execute(youtubeLink)

    }

    private lateinit var pDialog: ProgressDialog

    @SuppressLint("StaticFieldLeak")
    inner class RequestDownloadVideoStream : AsyncTask<String?, String?, String?>() {


        override fun onPreExecute() {
            super.onPreExecute()

            pDialog = ProgressDialog(requireContext())
            pDialog.setMessage("Caching video. Please wait...")
            pDialog.setIndeterminate(false)
            pDialog.setMax(100)
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            pDialog.setCancelable(false)
            pDialog.show()
        }

        override fun doInBackground(vararg params: String?): String? {
            var `is`: InputStream? = null
            val u: URL?

            var temp_progress = 0
            var progress = 0

            var len1: Int
            try {
                u = URL(params[0])
                `is` = u.openStream()
                val huc: URLConnection = u.openConnection() as URLConnection
                huc.connect()
                val size = huc.contentLength
                val fileName = params[1] + params[2]
                val storagePath: String =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toString() + "/YoutubeVideos"
                Log.d("TAG", "++++++++++: $storagePath")
                val f = File(storagePath)
                if (!f.exists()) {
                    f.mkdirs()
                }

                val fileNameEdit = fileName.replace("[/#@%\$*+&,;'●|=]".toRegex(), " ")

                val fos = FileOutputStream("$f/$fileNameEdit", true)
                val buffer = ByteArray(1024)
                var total = 0
                if (`is` != null) {
                    while (`is`.read(buffer).also { len1 = it } != -1) {
                        total += len1
                        // publishing the progress....
                        // After this onProgressUpdate will be called
                        if(size != 0) {
                            progress = (total * 100 / size)
                            if (progress >= 0) {
                                temp_progress = progress
                                publishProgress("" + progress)
                            } else
                                publishProgress("" + temp_progress + 1)
                            fos.write(buffer, 0, len1)
                        }
                    }
                }
                publishProgress("" + 100)
                fos.close()
                Snackbar.make(requireView(), "The Video is Cached", Snackbar.LENGTH_SHORT).show()
            }catch (e: MalformedURLException) {
                Log.d("TAG", "****MalformedURLException: $e")
                Snackbar.make(requireView(), "$e", Snackbar.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("TAG", "****IOException: $e")
                Snackbar.make(requireView(), "$e", Snackbar.LENGTH_SHORT).show()
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Snackbar.make(requireView(), "$e", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            return null
        }


        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)

            pDialog.progress = values[0]!!.toInt()
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            if (pDialog.isShowing) pDialog.dismiss()
        }



    }


}