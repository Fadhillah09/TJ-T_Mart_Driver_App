package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val status: String?,
    val token: String?,
    val message: String?,
    val user: UserData?
) {
    data class UserData(
        val id: Int,
        val name: String,
        val email: String,
        @SerializedName("no_telp") val noTelp: String?
    )
}