package com.example.cachetube.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cachetube.MusicService
import com.example.cachetube.R
import com.example.cachetube.data.Playlist
import com.example.cachetube.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (!checkPermissionForReadExternalStorage()) {
            try {
                requestPermissionForReadExternalStorage()
            } catch (e: Exception) { }
        }

        binding.bottomNavMenu.setupWithNavController(navHostFragment.findNavController())

        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.searchFragment, R.id.playlistsFragment, R.id.nowPlayingFragment, R.id.favouritesFragment, R.id.mySongsFragment, R.id.singlePlaylistFragment ->
                    binding.bottomNavMenu.visibility = View.VISIBLE
                else ->
                    binding.bottomNavMenu.visibility = View.GONE
            }
        }


        openPlayerWhenNotificationIsTapped()

    }





    override fun onDestroy() {
        super.onDestroy()

        val intent = Intent(this, MusicService::class.java)
        stopService(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        openPlayerWhenNotificationIsTapped()
    }



    private fun openPlayerWhenNotificationIsTapped(){
        if(intent.action == "notification_tap"){
            val action = SearchFragmentDirections.actionSearchFragmentToNowPlayingFragment(false, 0, false, false, Playlist("" , mutableListOf()))
            navHostFragment.findNavController().navigate(action)
        }
    }





    private fun requestPermissionForReadExternalStorage() {
        ActivityCompat.requestPermissions(this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            99
        )
    }

    private fun checkPermissionForReadExternalStorage(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val result: Int = this.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        val result2: Int = this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED
    }
    return false
}



}
