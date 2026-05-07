package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model

import com.google.gson.annotations.SerializedName

data class UpdateStatusResponse(
    val status: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)
