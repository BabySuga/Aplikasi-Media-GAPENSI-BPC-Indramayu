package com.example.android_user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Data Class untuk merepresentasikan Anggota dari Backend
data class Member(
    val id: String,
    @SerializedName("nama_perusahaan") val namaPerusahaan: String?,
    val nama: String?,
    val nik: String?,
    val email: String?,
    @SerializedName("tempat_lahir") val tempatLahir: String?,
    @SerializedName("tanggal_lahir") val tanggalLahir: String?,
    @SerializedName("jenis_kelamin") val jenisKelamin: String?,
    @SerializedName("golongan_darah") val golonganDarah: String?,
    val alamat: String?,
    val rt: String?,
    val rw: String?,
    @SerializedName("kelurahan_atau_desa") val kelurahanAtauDesa: String?,
    val kecamatan: String?,
    val agama: String?,
    @SerializedName("status_perkawinan") val statusPerkawinan: String?,
    val pekerjaan: String?,
    val kewarganegaraan: String?,
    val username: String?,
    val status: String?,
    @SerializedName("created_at") val createdAt: String?
)

class DaftarAnggota : BaseActivity() {
    private lateinit var recyclerViewMembers: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_anggota)

        recyclerViewMembers = findViewById(R.id.recyclerViewMembers)
        progressBar = findViewById(R.id.progressBar)

        // Setup RecyclerView
        recyclerViewMembers.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        membersAdapter = MembersAdapter()
        recyclerViewMembers.adapter = membersAdapter

        fetchApprovedMembers()
    }

    private fun fetchApprovedMembers() {
        progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        apiService.getApprovedMembers().enqueue(object : Callback<List<Member>> {
            override fun onResponse(call: Call<List<Member>>, response: Response<List<Member>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val approvedMembers = response.body()
                    if (!approvedMembers.isNullOrEmpty()) {
                        membersAdapter.submitList(approvedMembers)
                        Log.d("DaftarAnggota", "Data anggota berhasil dimuat: ${approvedMembers.size} anggota.")
                    } else {
                        Log.d("DaftarAnggota", "Tidak ada anggota yang disetujui.")
                        Toast.makeText(this@DaftarAnggota, R.string.no_members_found, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = "Gagal mengambil anggota: ${response.code()} - ${response.message()}"
                    Log.e("DaftarAnggota", errorMessage)
                    Toast.makeText(this@DaftarAnggota, R.string.failed_load_members, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Member>>, t: Throwable) {
                progressBar.visibility = View.GONE
                val errorMessage = "Koneksi Gagal saat mengambil anggota: ${t.message}"
                Log.e("DaftarAnggota", errorMessage, t)
                Toast.makeText(this@DaftarAnggota, getString(R.string.connection_failed_members, t.message), Toast.LENGTH_LONG).show()
            }
        })
    }
}

// Adapter untuk RecyclerView dengan DiffUtil
class MembersAdapter : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    private var members: List<Member> = emptyList()

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNo: TextView = itemView.findViewById(R.id.textViewNo)
        val textViewBadanUsaha: TextView = itemView.findViewById(R.id.textViewBadanUsaha)
        val textViewPimpinan: TextView = itemView.findViewById(R.id.textViewPimpinan)
        val textViewAlamat: TextView = itemView.findViewById(R.id.textViewAlamat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member_table, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        val context = holder.itemView.context

        // Kolom No
        val noText = context.getString(R.string.number_format, position + 1)
        holder.textViewNo.text = noText
        holder.textViewNo.contentDescription = context.getString(R.string.no_column_description, noText)

        // Kolom Nama Perusahaan (Capitalize first letter of each word after PT. or CV.)
        val namaPerusahaan = member.namaPerusahaan?.toTitleCase() ?: "N/A"
        holder.textViewBadanUsaha.text = namaPerusahaan
        holder.textViewBadanUsaha.contentDescription = context.getString(R.string.nama_perusahaan_column_description, namaPerusahaan)

        // Kolom Nama Pimpinan
        val namaPimpinan = member.nama ?: "N/A"
        holder.textViewPimpinan.text = namaPimpinan
        holder.textViewPimpinan.contentDescription = context.getString(R.string.nama_pimpinan_column_description, namaPimpinan)

        // Kolom Alamat (Only alamat and kelurahanAtauDesa, with space after comma)
        val addressParts = mutableListOf<String>()
        member.alamat?.takeIf { it.isNotEmpty() }?.let { addressParts.add(it) }
        member.kelurahanAtauDesa?.takeIf { it.isNotEmpty() }?.let { addressParts.add(it) }
        val finalAddress = addressParts.joinToString(separator = ", ").ifEmpty { "N/A" }
        holder.textViewAlamat.text = finalAddress
        holder.textViewAlamat.contentDescription = context.getString(R.string.alamat_column_description, finalAddress)
    }

    override fun getItemCount(): Int = members.size

    fun submitList(newMembers: List<Member>) {
        val diffResult = DiffUtil.calculateDiff(MemberDiffCallback(this.members, newMembers))
        this.members = newMembers
        diffResult.dispatchUpdatesTo(this)
    }

    // Helper function to capitalize with PT./CV. standardization
    private fun String.toTitleCase(): String {
        val trimmed = this.trim()
        if (trimmed.isEmpty()) return ""

        val lowerInput = trimmed.lowercase()
        val words = trimmed.split(" ").filter { it.isNotEmpty() }

        return when {
            lowerInput.startsWith("pt") -> {
                val remainingWords = words.drop(1).joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                "PT. ${remainingWords.ifEmpty { "" }}"
            }
            lowerInput.startsWith("cv") -> {
                val remainingWords = words.drop(1).joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                "CV. ${remainingWords.ifEmpty { "" }}"
            }
            else -> {
                words.joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }
    }

    private class MemberDiffCallback(
        private val oldList: List<Member>,
        private val newList: List<Member>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldMember = oldList[oldItemPosition]
            val newMember = newList[newItemPosition]
            return oldMember.namaPerusahaan == newMember.namaPerusahaan &&
                    oldMember.nama == newMember.nama &&
                    oldMember.alamat == newMember.alamat &&
                    oldMember.rt == newMember.rt &&
                    oldMember.rw == newMember.rw &&
                    oldMember.kelurahanAtauDesa == newMember.kelurahanAtauDesa &&
                    oldMember.kecamatan == newMember.kecamatan &&
                    oldMember.agama == newMember.agama &&
                    oldMember.statusPerkawinan == newMember.statusPerkawinan &&
                    oldMember.pekerjaan == newMember.pekerjaan &&
                    oldMember.kewarganegaraan == newMember.kewarganegaraan &&
                    oldMember.username == newMember.username &&
                    oldMember.email == newMember.email &&
                    oldMember.createdAt == newMember.createdAt
        }
    }
}