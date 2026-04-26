package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api

import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {


    @POST("driver/login")
    fun loginDriver(@Body body: LoginRequest): Call<LoginResponse>

    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<MessageResponse>

    @GET("me")
    fun getMe(@Header("Authorization") token: String): Call<LoginResponse.UserData>

    @GET("driver/pesanan")
    fun getPesanan(@Header("Authorization") token: String): Call<PesananResponse>

    @POST("driver/pesanan/{id}/claim")
    fun claimPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<MessageResponse>

    @POST("driver/pesanan/{id}/update-status")
    fun selesaikanPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<MessageResponse>

    @GET("driver/omset")
    fun getOmset(@Header("Authorization") token: String): Call<OmsetResponse>

    @GET("driver/riwayat")
    fun getRiwayat(@Header("Authorization") token: String): Call<PesananResponse>
}