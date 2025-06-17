package com.example.android_user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import androidx.core.content.edit // Penting: Import ini untuk SharedPreferences KTX

class Login : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        registerText = findViewById(R.id.register_text)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                showErrorDialog("Username dan password tidak boleh kosong!")
                return@setOnClickListener
            }
            authenticateUserViaAPI(username, password)
        }

        registerText.setOnClickListener {
            val intent = Intent(this@Login, Register::class.java)
            startActivity(intent)
        }

        // Cek jika sudah ada user yang login (dari SharedPreferences)
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val loggedInUserId = sharedPref.getString("logged_in_user_id", null)
        if (loggedInUserId != null) {
            // Jika ada user ID di SharedPreferences, langsung redirect ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun authenticateUserViaAPI(username: String, password: String) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("${Config.BASE_URL}/login/")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showErrorDialog("Gagal terhubung ke server: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseJson = responseBody?.let { JSONObject(it) }
                        val retrievedUsername = responseJson?.getString("nama") // Ambil 'nama' dari respons backend
                        val retrievedUserId = responseJson?.getString("user_id") // Ambil 'user_id' dari respons backend

                        if (retrievedUsername != null && retrievedUserId != null) {
                            // Simpan username dan user_id ke SharedPreferences
                            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            sharedPref.edit { // Menggunakan ekstensi KTX 'edit'
                                putString("logged_in_username", retrievedUsername)
                                putString("logged_in_user_id", retrievedUserId)
                            }

                            // Lanjutkan ke MainActivity
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Tutup LoginActivity
                        } else {
                            showErrorDialog("Login berhasil, tetapi data username/user ID tidak lengkap dari server.")
                        }
                    } else {
                        val errorJson = responseBody?.let { JSONObject(it) }
                        val errorMessage = errorJson?.getString("detail") ?: "Terjadi kesalahan"
                        showErrorDialog(errorMessage)
                    }
                }
            }
        })
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}