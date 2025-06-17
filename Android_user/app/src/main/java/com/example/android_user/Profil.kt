package com.example.android_user // Pastikan package name Anda sesuai

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.webkit.WebView
import android.widget.Toast
import androidx.core.net.toUri

class Profil : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val webView: WebView = findViewById(R.id.webview_deskripsi)
        val rawHtml = getString(R.string.definisi_gapensi)

        val htmlWithStyle = """
            
                $rawHtml
            
        """.trimIndent()

        webView.loadDataWithBaseURL(null, htmlWithStyle, "text/html", "UTF-8", null)
    }

    fun downloadLogo(view: android.view.View) {
        // Ganti dengan URL publik atau presigned URL dari S3
        val downloadUrl = "https://elasticbeanstalk-ap-southeast-1-704885513627.s3.ap-southeast-1.amazonaws.com/logo_gapensi2.png" // Untuk publik
        val fileName = "logo_gapensi2.png"

        val request = DownloadManager.Request(downloadUrl.toUri())
            .setTitle(fileName)
            .setDescription("Mengunduh logo Gapensi")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Mengunduh logo, cek folder Downloads", Toast.LENGTH_SHORT).show()
    }
}