package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model

import com.google.gson.annotations.SerializedName

data class OmsetResponse(
    val status: String?,
    val data: OmsetData?
) {
    data class OmsetData(
        val saldo: Double,
        @SerializedName("nama_bank") val namaBank: String?,
        @SerializedName("nomor_rekening") val nomorRekening: String?,
        @SerializedName("tanggal_gaji") val tanggalGaji: String?
    )
}