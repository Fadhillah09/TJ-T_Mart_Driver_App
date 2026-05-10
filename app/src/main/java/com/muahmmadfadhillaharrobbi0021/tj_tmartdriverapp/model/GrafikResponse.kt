package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model

import com.google.gson.annotations.SerializedName

data class GrafikResponse(
    val status: String?,
    val data: List<GrafikItem>?
) {
    data class GrafikItem(
        val label: String?,
        @SerializedName("total") val total: Double
    )
}