package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model

import com.google.gson.annotations.SerializedName

data class Pesanan(
    val id: Int,
    @SerializedName("id_transaksi") val idTransaksi: String?,
    @SerializedName("total_harga") val totalHarga: Int,
    val status: String?,
    @SerializedName("status_antar") val statusAntar: String?,
    @SerializedName("tipe_layanan") val tipeLayanan: String?,
    @SerializedName("kurir_id") val kurirId: Int?,
    @SerializedName("metode_pembayaran") val metodePembayaran: String?,
    @SerializedName("created_at") val createdAt: String?,
    val user: UserPemesan?,
    @SerializedName("details") val details: List<ItemPesanan>?
) {
    data class ItemPesanan(
        @SerializedName("nama_produk") val namaProduk: String?,
        val qty: Int?,
        val harga: Int?
    )
    data class UserPemesan(
        val name: String?,
        @SerializedName("no_telp") val noTelp: String?,
        @SerializedName("alamat_gedung") val alamatGedung: String?,
        @SerializedName("nomor_kamar") val nomorKamar: String?,
        val lokasi: LokasiData?
    ) {
        fun getNamaLokasiLengkap(): String {
            if (lokasi?.namaLokasi != null) {
                return "${lokasi.namaLokasi}${if (nomorKamar != null) " - Kamar $nomorKamar" else ""}"
            }
            val gedung = alamatGedung ?: "Gedung -"
            val kamar = if (nomorKamar != null) " - Kamar $nomorKamar" else ""
            return "$gedung$kamar"
        }
    }

    data class LokasiData(
        val id: Int,
        @SerializedName("nama_lokasi") val namaLokasi: String?,
        @SerializedName("nama_gedung") val namaGedung: String?
    )
}