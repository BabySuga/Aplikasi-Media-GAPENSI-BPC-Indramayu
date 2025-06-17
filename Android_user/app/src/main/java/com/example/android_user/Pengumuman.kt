package com.example.android_user

import com.google.gson.annotations.SerializedName

data class Pengumuman(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("gambarUrl")
    val gambarUrl: String = "",

    @SerializedName("judul")
    val judul: String = "",

    @SerializedName("tanggal")
    val tanggal: String = "",

    @SerializedName("isi")
    val isi: String = ""
)