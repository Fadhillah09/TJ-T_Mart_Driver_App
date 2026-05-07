package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model

import com.google.gson.annotations.SerializedName

data class UpdateStatusRequest(
    @SerializedName("status_antar") val status_antar: String
)