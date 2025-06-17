package com.example.android_user

import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Fasilitas : BaseActivity() {

    private lateinit var imageViewLobby1: ImageView
    private lateinit var imageViewMeeting1: ImageView
    private lateinit var imageViewKetum1: ImageView
    private lateinit var imageViewKetum2: ImageView
    private lateinit var imageViewRuangKerja: ImageView
    private lateinit var imageViewLobi: ImageView
    private lateinit var imageViewMushola: ImageView
    private lateinit var imageViewTempatWudhu: ImageView

    // Variabel untuk menyimpan data gambar saat meminta izin
    private var pendingDownloadFileName: String? = null
    private var pendingDownloadUrl: String? = null

    companion object {
        private const val REQUEST_WRITE_STORAGE_PERMISSION = 101
        // Daftar URL eksplisit untuk setiap gambar dari bucket S3
        private val IMAGE_URLS = listOf(
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/gedungdepan.jpg", // fasilitas_lobby_1
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/ruangmeeting.jpg", // fasilitas_meeting_1
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/ruangketum.jpg", // fasilitas_ketum_1
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/ruangketum2.jpg", // fasilitas_ketum_2
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/ruangkerja.jpg", // fasilitas_ruangkerja
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/lobi.jpg", // fasilitas_lobi
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/mushola1.jpg", // fasilitas_mushola
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/tempatwudhu.jpg" // fasilitas_tempatwudhu
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fasilitas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi ImageViews sesuai ID di activity_fasilitas.xml
        imageViewLobby1 = findViewById(R.id.fasilitas_lobby_1)
        imageViewMeeting1 = findViewById(R.id.fasilitas_meeting_1)
        imageViewKetum1 = findViewById(R.id.fasilitas_ketum_1)
        imageViewKetum2 = findViewById(R.id.fasilitas_ketum_2)
        imageViewRuangKerja = findViewById(R.id.fasilitas_ruangkerja)
        imageViewLobi = findViewById(R.id.fasilitas_lobi)
        imageViewMushola = findViewById(R.id.fasilitas_mushola)
        imageViewTempatWudhu = findViewById(R.id.fasilitas_tempatwudhu)

        // Set OnClickListener untuk setiap ImageView
        setupImageViewInteractions(imageViewLobby1, "gedungdepan.jpg", IMAGE_URLS[0])
        setupImageViewInteractions(imageViewMeeting1, "ruangmeeting.jpg", IMAGE_URLS[1])
        setupImageViewInteractions(imageViewKetum1, "ruangketum.jpg", IMAGE_URLS[2])
        setupImageViewInteractions(imageViewKetum2, "ruangketum2.jpg", IMAGE_URLS[3])
        setupImageViewInteractions(imageViewRuangKerja, "ruangkerja.jpg", IMAGE_URLS[4])
        setupImageViewInteractions(imageViewLobi, "lobi.jpg", IMAGE_URLS[5])
        setupImageViewInteractions(imageViewMushola, "mushola1.jpg", IMAGE_URLS[6])
        setupImageViewInteractions(imageViewTempatWudhu, "tempatwudhu.jpg", IMAGE_URLS[7])
    }

    private fun setupImageViewInteractions(imageView: ImageView, defaultFileName: String, downloadUrl: String) {
        imageView.setOnClickListener {
            imageView.drawable?.let { drawable ->
                showImageFullScreen(drawable, defaultFileName, downloadUrl)
            } ?: run {
                Toast.makeText(this, "Gambar belum dimuat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageFullScreen(drawable: Drawable, fileName: String, downloadUrl: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_fullscreen_image)

        val imageViewFullScreen: ImageView = dialog.findViewById(R.id.imageViewFullScreen)
        imageViewFullScreen.setImageDrawable(drawable)

        val btnClose: ImageButton = dialog.findViewById(R.id.btnClose)
        val btnDownload: ImageButton = dialog.findViewById(R.id.btnDownload)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnDownload.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE_PERMISSION)
                    pendingDownloadFileName = fileName
                    pendingDownloadUrl = downloadUrl
                    return@setOnClickListener
                }
            }
            startDownload(downloadUrl, fileName)
            Toast.makeText(this, "Mengunduh gambar, cek folder Downloads", Toast.LENGTH_SHORT).show()
        }

        val layoutRootFullScreen: View = dialog.findViewById(R.id.layoutRootFullScreen)
        layoutRootFullScreen.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.show()
    }

    private fun startDownload(url: String, fileName: String) {
        val request = DownloadManager.Request(url.toUri())
            .setTitle(fileName)
            .setDescription("Mengunduh gambar dari Fasilitas")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pendingDownloadUrl?.let { url ->
                    pendingDownloadFileName?.let { fileName ->
                        startDownload(url, fileName)
                        Toast.makeText(this, "Mengunduh gambar, cek folder Downloads", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Izin penyimpanan ditolak. Tidak dapat mengunduh gambar.", Toast.LENGTH_LONG).show()
            }
            pendingDownloadFileName = null
            pendingDownloadUrl = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}