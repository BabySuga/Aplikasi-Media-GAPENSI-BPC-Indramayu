package com.example.android_user

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings // Import WebSettings
import android.webkit.WebView // Import WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PengumumanAdapter(private val context: Context) :
    ListAdapter<Pengumuman, PengumumanAdapter.ViewHolder>(PengumumanDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pengumuman, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gambar: ImageView = itemView.findViewById(R.id.gambarImageView)
        private val judul: TextView = itemView.findViewById(R.id.judulTextView)
        private val tanggal: TextView = itemView.findViewById(R.id.tanggalTextView)
        private val isi: TextView = itemView.findViewById(R.id.isiTextView)
        private val btnSelengkapnya: Button = itemView.findViewById(R.id.btnSelengkapnya)

        fun bind(data: Pengumuman) {
            Glide.with(context)
                .load(data.gambarUrl)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.sample_pdf)
                .into(gambar)

            judul.text = data.judul
            tanggal.text = data.tanggal

            if (data.isi.length > 100) {
                // Since data.isi now contains HTML, truncating it directly might break tags.
                // For simplicity, if you need to truncate, consider truncating the raw text
                // before HTML conversion, or use a library that can truncate HTML safely.
                // For now, let's assume if there's HTML, we show it fully.
                isi.text = context.getString(R.string.truncated_text, stripHtml(data.isi).substring(0, 100))
            } else {
                isi.text = stripHtml(data.isi) // Tampilkan versi teks biasa di sini
            }

            btnSelengkapnya.setOnClickListener {
                Log.d("PengumumanAdapter", "Tombol 'Selengkapnya' diklik untuk judul: ${data.judul}")
                try {
                    val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                    dialog.setContentView(R.layout.dialog_fullscreen_pengumuman)
                    Log.d("PengumumanAdapter", "Dialog fullscreen layout berhasil di-inflate.")

                    val fullscreenGambar = dialog.findViewById<ImageView>(R.id.fullscreen_gambarImageView)
                    val fullscreenJudul = dialog.findViewById<TextView>(R.id.fullscreen_judulTextView)
                    val fullscreenTanggal = dialog.findViewById<TextView>(R.id.fullscreen_tanggalTextView)
                    val fullscreenIsiWebView = dialog.findViewById<WebView>(R.id.fullscreen_isiWebView)
                    val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)

                    // Logging for null checks (good practice)
                    if (fullscreenGambar == null) Log.e("PengumumanAdapter", "fullscreenGambar ImageView not found!")
                    if (fullscreenJudul == null) Log.e("PengumumanAdapter", "fullscreenJudul TextView not found!")
                    if (fullscreenTanggal == null) Log.e("PengumumanAdapter", "fullscreenTanggal TextView not found!")
                    if (fullscreenIsiWebView == null) Log.e("PengumumanAdapter", "fullscreenIsiWebView WebView not found!")
                    if (btnClose == null) Log.e("PengumumanAdapter", "btnClose ImageButton not found!")

                    Glide.with(context)
                        .load(data.gambarUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.sample_pdf)
                        .into(fullscreenGambar)

                    // --- Kapitalisasi Judul saat ditampilkan di dialog ---
                    fullscreenJudul.text = capitalizeWords(data.judul)

                    fullscreenTanggal.text = data.tanggal

                    // --- Memuat isi ke WebView dengan justify ---
                    fullscreenIsiWebView?.settings?.javaScriptEnabled = false
                    fullscreenIsiWebView?.setBackgroundColor(0)

                    // Langsung gunakan data.isi yang sudah diformat HTML dari backend
                    val htmlContent = """
                        <html>
                        <head>
                            <style type="text/css">
                                body {
                                    font-family: sans-serif;
                                    text-align: justify;
                                    color: #000000;
                                    font-size: 16px;
                                    margin: 0;
                                    padding: 0;
                                }
                                /* Style for paragraph spacing */
                                p {
                                    margin-bottom: 1em; /* Add space between paragraphs */
                                }
                                /* If you replaced \n\n with </p><p> without wrapping in a single <p>, adjust here.
                                   Current JS wraps entire content in one <p>, so <br> is used for single newlines.
                                   If you use `</p><p>` for paragraphs, the HTML would just be `data.isi` directly.
                                */
                            </style>
                        </head>
                        <body>
                            ${data.isi} <!-- Langsung sisipkan konten HTML dari data.isi -->
                        </body>
                        </html>
                    """.trimIndent()
                    fullscreenIsiWebView?.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)

                    btnClose?.setOnClickListener {
                        Log.d("PengumumanAdapter", "Tombol 'Close' di dialog fullscreen diklik.")
                        dialog.dismiss()
                    } ?: Log.e("PengumumanAdapter", "btnClose is null, cannot set OnClickListener.")

                    dialog.show()
                    Log.d("PengumumanAdapter", "Dialog fullscreen ditampilkan.")
                } catch (e: Exception) {
                    Log.e("PengumumanAdapter", "Terjadi kesalahan saat menampilkan dialog fullscreen pengumuman.", e)
                    Toast.makeText(context, "Gagal menampilkan detail pengumuman: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Fungsi helper untuk kapitalisasi setiap awal huruf per kata
    private fun capitalizeWords(str: String): String {
        return str.split(" ").joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
    }

    // Fungsi helper untuk menghapus tag HTML dasar (untuk tampilan singkat di item list)
    private fun stripHtml(html: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(html).toString()
        }
    }

    class PengumumanDiffCallback : DiffUtil.ItemCallback<Pengumuman>() {
        override fun areItemsTheSame(oldItem: Pengumuman, newItem: Pengumuman): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pengumuman, newItem: Pengumuman): Boolean {
            return oldItem == newItem
        }
    }
}