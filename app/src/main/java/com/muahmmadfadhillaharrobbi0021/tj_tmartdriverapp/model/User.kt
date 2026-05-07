package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model
import com.google.gson.annotations.SerializedName

data class User(
    val id: Int? = null,
    val name: String? = null,

    @SerializedName("no_telp")
    val phone: String? = null,

    @SerializedName("telepon")
    val telepon: String? = null,

    val alamatGedung: String? = null
)