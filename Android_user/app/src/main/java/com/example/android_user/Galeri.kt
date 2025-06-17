package com.example.android_user //

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

class Galeri : BaseActivity() {

    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var imageView5: ImageView
    private lateinit var imageView6: ImageView
    private lateinit var imageView7: ImageView
    private lateinit var imageView8: ImageView
    private lateinit var imageView9: ImageView
    private lateinit var imageView10: ImageView

    // Variabel untuk menyimpan data gambar saat meminta izin
    private var pendingDownloadFileName: String? = null
    private var pendingDownloadUrl: String? = null

    companion object {
        private const val REQUEST_WRITE_STORAGE_PERMISSION = 101
        // Daftar URL eksplisit untuk setiap gambar
        private val IMAGE_URLS = listOf(
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri1.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri2.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri3.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri4.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri5.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri6.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri7.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri8.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri9.jpg",
            "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/src/galeri10.jpg"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_galeri)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi ImageViews
        imageView1 = findViewById(R.id.galeri_image_1)
        imageView2 = findViewById(R.id.galeri_image_2)
        imageView3 = findViewById(R.id.galeri_image_3)
        imageView4 = findViewById(R.id.galeri_image_4)
        imageView5 = findViewById(R.id.galeri_image_5)
        imageView6 = findViewById(R.id.galeri_image_6)
        imageView7 = findViewById(R.id.galeri_image_7)
        imageView8 = findViewById(R.id.galeri_image_8)
        imageView9 = findViewById(R.id.galeri_image_9)
        imageView10 = findViewById(R.id.galeri_image_10)

        // Set OnClickListener untuk setiap ImageView
        setupImageViewInteractions(imageView1, "galeri1.jpg", IMAGE_URLS[0])
        setupImageViewInteractions(imageView2, "galeri2.jpg", IMAGE_URLS[1])
        setupImageViewInteractions(imageView3, "galeri3.jpg", IMAGE_URLS[2])
        setupImageViewInteractions(imageView4, "galeri4.jpg", IMAGE_URLS[3])
        setupImageViewInteractions(imageView5, "galeri5.jpg", IMAGE_URLS[4])
        setupImageViewInteractions(imageView6, "galeri6.jpg", IMAGE_URLS[5])
        setupImageViewInteractions(imageView7, "galeri7.jpg", IMAGE_URLS[6])
        setupImageViewInteractions(imageView8, "galeri8.jpg", IMAGE_URLS[7])
        setupImageViewInteractions(imageView9, "galeri9.jpg", IMAGE_URLS[8])
        setupImageViewInteractions(imageView10, "galeri10.jpg", IMAGE_URLS[9])
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
            .setDescription("Mengunduh gambar dari Galeri")
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