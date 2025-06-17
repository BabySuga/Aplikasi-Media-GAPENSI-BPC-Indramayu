package com.example.android_user

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
// Hapus import com.google.firebase.auth.FirebaseAuth jika Anda tidak menggunakannya sama sekali
// Hapus import com.google.firebase.auth.FirebaseUser jika Anda tidak menggunakannya sama sekali
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.content.edit // Tambahkan import ini untuk SharedPreferences KTX

class MainActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengumumanAdapter
    private lateinit var bannerSlider: ViewPager2
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var userNameTextView: TextView
    // Hapus private lateinit var auth: FirebaseAuth jika Anda tidak menggunakannya sama sekali
    private lateinit var userDatabase: DatabaseReference
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hapus inisialisasi FirebaseAuth jika tidak digunakan
        // auth = FirebaseAuth.getInstance()

        userNameTextView = findViewById(R.id.userNameTextView)
        progressBar = findViewById(R.id.progressBar)
        bannerSlider = findViewById(R.id.bannerSlider)
        recyclerView = findViewById(R.id.recyclerViewPengumuman)

        updateUserNameFromPrefs()

        val bannerImages = listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)
        bannerAdapter = BannerAdapter(bannerImages)
        bannerSlider.adapter = bannerAdapter
        setupBannerAutoSlide()

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = PengumumanAdapter(this)
        recyclerView.adapter = adapter

        fetchLatestPengumuman()
        setupNavigationButtons()
    }

    private fun updateUserNameFromPrefs() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("logged_in_username", null)
        val userId = sharedPref.getString("logged_in_user_id", null)

        if (!username.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            Log.d("MainActivity", "User logged in via Prefs: Username=$username, UserID=$userId")
            userNameTextView.text = username

            userDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("MainActivity", "Firebase RTDB data for user: ${snapshot.value}")
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to load additional user data from Firebase RTDB: ${error.message}")
                }
            })

        } else {
            Log.d("MainActivity", "No user data found in SharedPreferences, redirecting to Login.")
            userNameTextView.text = getString(R.string.guest_user)
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun fetchLatestPengumuman() {
        progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getAnnouncements().enqueue(object : Callback<List<Pengumuman>> {
            override fun onResponse(call: Call<List<Pengumuman>>, response: Response<List<Pengumuman>>) { // Corrected method name
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val allAnnouncements = response.body()
                    val latestAnnouncement = allAnnouncements?.sortedByDescending { it.tanggal }?.take(1)
                    if (!latestAnnouncement.isNullOrEmpty()) {
                        adapter.submitList(latestAnnouncement)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal mengambil pengumuman.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Pengumuman>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupNavigationButtons() {
        findViewById<View>(R.id.button_pengumuman).setOnClickListener {
            startActivity(Intent(this, PengumumanActivity::class.java))
        }
        findViewById<View>(R.id.button_profil).setOnClickListener {
            startActivity(Intent(this, Profil::class.java))
        }
        findViewById<View>(R.id.button_struktur_organisasi).setOnClickListener {
            startActivity(Intent(this, StrukturOrg::class.java))
        }
        findViewById<View>(R.id.button_daftar_anggota).setOnClickListener {
            startActivity(Intent(this, DaftarAnggota::class.java))
        }
        findViewById<View>(R.id.button_galeri).setOnClickListener {
            startActivity(Intent(this, Galeri::class.java))
        }
        findViewById<View>(R.id.button_fasilitas).setOnClickListener {
            startActivity(Intent(this, Fasilitas::class.java))
        }
        findViewById<View>(R.id.button_kontak).setOnClickListener {
            startActivity(Intent(this, Kontak::class.java))
        }
        findViewById<View>(R.id.button_logout).setOnClickListener {
            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            sharedPref.edit { // Menggunakan ekstensi KTX 'edit'
                clear()
            }
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun setupBannerAutoSlide() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val currentItem = bannerSlider.currentItem
                val nextItem = if (currentItem < bannerAdapter.itemCount - 1) currentItem + 1 else 0
                bannerSlider.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 4000)
            }
        }
        handler.postDelayed(runnable, 4000)
    }
}