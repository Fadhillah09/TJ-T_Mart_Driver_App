package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model

import com.google.gson.annotations.SerializedName

data class RewardResponse(
    val status: String?,
    val message: String?,
    val data: RewardData?
) {
    data class RewardData(
        @SerializedName("pesanan_bulan_ini") val pesananBulanIni: Int?,
        @SerializedName("target") val target: Int?,
        @SerializedName("sudah_klaim") val sudahKlaim: Boolean?,
        @SerializedName("bisa_klaim") val bisaKlaim: Boolean?
    )
}