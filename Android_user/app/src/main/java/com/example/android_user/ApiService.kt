package com.example.android_user

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    // Endpoint Pengumuman
    @GET("get-announcements/")
    fun getAnnouncements(): Call<List<Pengumuman>>

    // Endpoint Member
    @GET("approved-members/")
    fun getApprovedMembers(): Call<List<Member>>
}