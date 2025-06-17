package com.example.android_user

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ini yang membuat konten menggambar di belakang status/nav bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set warna status bar menjadi putih (opsional, bisa dari tema)
        // Jika Anda menggunakan theming untuk status bar, Anda bisa hapus baris ini
        @Suppress("DEPRECATION") // Ini untuk peringatan deprecation statusBarColor
        window.statusBarColor = Color.WHITE

        val insetsController: WindowInsetsControllerCompat? = WindowCompat.getInsetsController(window, window.decorView)

        // Mengatur ikon status bar agar menjadi gelap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insetsController?.isAppearanceLightStatusBars = true
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}