package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api

import com.google.gson.GsonBuilder
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC // Ganti BODY → BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)  // Naikkan jadi 60
                .writeTimeout(60, TimeUnit.SECONDS) // Tambah writeTimeout
                .build()

            val gson = GsonBuilder()
                .setLenient() // Toleran terhadap JSON besar/tidak sempurna
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
        }
        return retrofit!!
    }

    val instance: ApiService by lazy {
        getClient().create(ApiService::class.java)
    }
}