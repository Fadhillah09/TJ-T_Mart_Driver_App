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
    @SerializedName("updated_at") val updatedAt: String?,
    val user: UserPemesan?,
    @SerializedName("details") val details: List<ItemPesanan>?,
    @SerializedName("alamat_display") val alamatDisplay: String?,
    @SerializedName("pembayaran_display") val pembayaranDisplay: String?,
    @SerializedName("nama_mart") val namaMart: String?,
    @SerializedName("jarak") val jarak: String?,
    @SerializedName("durasi") val durasi: String?,
    @SerializedName("ongkir") val ongkir: Int?,
    @SerializedName("biaya_layanan") val biayaLayanan: Int?
) {
    data class ItemPesanan(
        @SerializedName("nama_produk") val namaProduk: String?,
        val qty: Int?,
        val jumlah: Int?,
        @SerializedName("harga_satuan") val harga: Int?,
        val subtotal: Int?,
        @SerializedName("foto_produk") val fotoProduk: String?
    )

    data class UserPemesan(
        val id: Int?,
        val name: String?,
        @SerializedName("no_telp") val noTelp: String?,
        @SerializedName("lokasi_id") val lokasiId: Int?,       // ← tambah ini
        @SerializedName("nomor_kamar") val nomorKamar: String?,
        @SerializedName("alamat_gedung") val alamatGedung: String?,
        val gambar: String?,
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
        @SerializedName("nama_gedung") val namaGedung: String?,
        val jarak: String?,
        val durasi: String?,
        @SerializedName("nama_mart") val namaMart: String?,
    )
}