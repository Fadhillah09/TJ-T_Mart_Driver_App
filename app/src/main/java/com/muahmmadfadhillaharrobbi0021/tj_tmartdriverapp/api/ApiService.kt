package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api

import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Headers("Accept: application/json")
    @POST("driver/login")
    fun loginDriver(@Body body: LoginRequest): Call<LoginResponse>

    @Headers("Accept: application/json")
    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<MessageResponse>

    @Headers("Accept: application/json")
    @GET("me")
    fun getMe(@Header("Authorization") token: String): Call<LoginResponse.UserData>

    @Headers("Accept: application/json")
    @GET("driver/pesanan")
    fun getPesanan(@Header("Authorization") token: String): Call<PesananResponse>

    @Headers("Accept: application/json")
    @POST("driver/pesanan/{id}/claim")
    fun claimPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<MessageResponse>

    @Headers("Accept: application/json")
    @POST("driver/pesanan/{id}/update-status")
    fun selesaikanPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<MessageResponse>

    @Headers("Accept: application/json")
    @POST("driver/pesanan/{id}/batalkan")
    fun batalkanPesanan(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Call<MessageResponse>

    @Headers("Accept: application/json")
    @GET("driver/omset")
    fun getOmset(@Header("Authorization") token: String): Call<OmsetResponse>

    @Headers("Accept: application/json")
    @GET("driver/riwayat")
    fun getRiwayat(@Header("Authorization") token: String): Call<PesananResponse>

    @Headers("Accept: application/json")
    @PATCH("pesanan/{id}/status-antar")
    fun updateStatusAntar(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: UpdateStatusRequest
    ): Call<UpdateStatusResponse>
    @Headers("Accept: application/json")
    @POST("driver/absensi")
    fun prosesAbsen(
        @Header("Authorization") token: String,
        @Query("koordinat") koordinat: String
    ): Call<MessageResponse>
    @FormUrlEncoded
    @POST("driver/checkout") // Sesuaikan dengan route di Laravel kamu
    fun submitCheckout(
        @Header("Authorization") token: String,
        @Field("koordinat") koordinat: String
    ): Call<MessageResponse>

}