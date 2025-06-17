package com.example.android_user

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log

class PengumumanActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengumumanAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengumuman)

        recyclerView = findViewById(R.id.recyclerViewPengumuman)
        progressBar = findViewById(R.id.progressBar)

        adapter = PengumumanAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchPengumumanFromApi()
    }

    private fun fetchPengumumanFromApi() {
        progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.getAnnouncements()

        call.enqueue(object : Callback<List<Pengumuman>> {
            override fun onResponse(call: Call<List<Pengumuman>>, response: Response<List<Pengumuman>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val announcements = response.body()
                    if (!announcements.isNullOrEmpty()) {
                        adapter.submitList(announcements)
                        Log.d("PengumumanActivity", "Data pengumuman berhasil dimuat: ${announcements.size} pengumuman.")
                    } else {
                        Toast.makeText(this@PengumumanActivity, "Tidak ada data pengumuman.", Toast.LENGTH_SHORT).show()
                        Log.d("PengumumanActivity", "Tidak ada data pengumuman.")
                    }
                } else {
                    val errorMessage = "Gagal mengambil data: ${response.code()} - ${response.message()}"
                    Toast.makeText(this@PengumumanActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("PengumumanActivity", errorMessage)
                }
            }

            override fun onFailure(call: Call<List<Pengumuman>>, t: Throwable) {
                progressBar.visibility = View.GONE
                val errorMessage = "Koneksi Gagal: ${t.message}"
                Toast.makeText(this@PengumumanActivity, errorMessage, Toast.LENGTH_LONG).show()
                Log.e("PengumumanActivity", errorMessage, t)
            }
        })
    }
}