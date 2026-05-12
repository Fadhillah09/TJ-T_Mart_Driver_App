package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.riwayat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemRiwayatFullBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.Constants
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Sealed class untuk dua tipe item di RecyclerView: header tanggal & card pesanan
sealed class RiwayatItem {
    data class Header(val tanggal: String) : RiwayatItem()
    data class Card(val pesanan: Pesanan) : RiwayatItem()
}

class RiwayatFullAdapter(
    private val list: List<Pesanan>,
    private val onSelesaiClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_CARD = 1
        const val MAX_ITEM_SHOWN = 3
    }

    private val items: List<RiwayatItem> = buildItems(list)

    private fun buildItems(pesananList: List<Pesanan>): List<RiwayatItem> {
        val result = mutableListOf<RiwayatItem>()
        val grouped = pesananList.groupBy {
            formatTanggal(it.createdAt ?: it.updatedAt)
        }
        for ((tanggal, items) in grouped) {
            result.add(RiwayatItem.Header(tanggal))
            items.forEach { result.add(RiwayatItem.Card(it)) }
        }
        return result
    }

    private fun formatTanggal(raw: String?): String {
        if (raw == null) return "Tanggal tidak diketahui"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.isLenient = true
            val date = sdf.parse(raw) ?: return raw
            val outSdf = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
            outSdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            outSdf.format(date)
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf2.timeZone = TimeZone.getTimeZone("UTC")
                sdf2.isLenient = true
                val date = sdf2.parse(raw) ?: return raw
                val outSdf2 = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
                outSdf2.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                outSdf2.format(date)
            } catch (e2: Exception) {
                raw.take(10)
            }
        }
    }

    private fun formatJam(raw: String?): String {
        if (raw == null) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.isLenient = true
            val date = sdf.parse(raw) ?: return ""
            val outSdf = SimpleDateFormat("HH.mm 'WIB'", Locale.getDefault())
            outSdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            outSdf.format(date)
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf2.timeZone = TimeZone.getTimeZone("UTC")
                sdf2.isLenient = true
                val date = sdf2.parse(raw) ?: return ""
                val outSdf2 = SimpleDateFormat("HH.mm 'WIB'", Locale.getDefault())
                outSdf2.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                outSdf2.format(date)
            } catch (e2: Exception) { "" }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is RiwayatItem.Header -> TYPE_HEADER
            is RiwayatItem.Card -> TYPE_CARD
        }
    }

    // ViewHolder Header Tanggal
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggal: android.widget.TextView = itemView.findViewById(R.id.tvTanggalHeader)
    }

    // ViewHolder Card Pesanan
    inner class CardViewHolder(val binding: ItemRiwayatFullBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tanggal_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemRiwayatFullBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CardViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is RiwayatItem.Header -> {
                (holder as HeaderViewHolder).tvTanggal.text = item.tanggal
            }
            is RiwayatItem.Card -> {
                val pesanan = item.pesanan
                Log.d("DEBUG_PESANAN", pesanan.toString())
                val b = (holder as CardViewHolder).binding
                Log.d("TANGGAL_RAW", "createdAt = ${pesanan.createdAt}")

                val context = b.root.context
                val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                nf.maximumFractionDigits = 0

                // Rute
                b.tvOrigin.text = pesanan.namaMart ?: pesanan.user?.lokasi?.namaMart ?: "TJ Mart Putri"
                b.tvDestination.text = pesanan.alamatDisplay ?: pesanan.user?.getNamaLokasiLengkap() ?: "-"
                b.tvWaktu.text = formatJam(pesanan.createdAt ?: pesanan.updatedAt)

                // Nama pelanggan & jumlah item
                b.tvNamaPelanggan.text = pesanan.user?.name ?: "Pelanggan"
                // FIX: jumlah pakai jumlah ?: qty (backend riwayat hanya kirim jumlah, bukan qty)
                val jumlahItem = pesanan.details?.sumOf { it.jumlah ?: it.qty ?: 0 } ?: 0
                b.tvJumlahPesanan.text = "Total : $jumlahItem Pesanan"

                // Load foto pelanggan
                val fotoUrl = pesanan.user?.gambar
                Log.d("FOTO_URL", "gambar = $fotoUrl")

                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(context)
                        .load("${Constants.BASE_URL}storage/$fotoUrl")
                        .placeholder(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(b.ivAvatarUser)
                } else {
                    b.ivAvatarUser.setImageResource(R.drawable.ic_default_avatar)
                }

                // Status badge
                when (pesanan.statusAntar?.lowercase()) {
                    "selesai", "tiba" -> {
                        b.tvStatusBadge.text = "Selesai"
                        b.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_selesai)
                        b.tvStatusBadge.setTextColor(context.getColor(R.color.white))
                        b.btnSelesaikan.visibility = View.GONE
                    }
                    "dibatalkan" -> {
                        b.tvStatusBadge.text = "Dibatalkan"
                        b.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_dibatalkan)
                        b.tvStatusBadge.setTextColor(context.getColor(R.color.white))
                        b.btnSelesaikan.visibility = View.GONE
                    }
                    "sedang diantar" -> {
                        b.tvStatusBadge.text = "Belum selesai"
                        b.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_proses)
                        b.tvStatusBadge.setTextColor(context.getColor(R.color.white))
                        b.btnSelesaikan.visibility = View.VISIBLE
                        b.btnSelesaikan.setOnClickListener { onSelesaiClick(pesanan.id) }
                    }
                    else -> {
                        b.tvStatusBadge.text = "Selesai"
                        b.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_selesai)
                        b.tvStatusBadge.setTextColor(context.getColor(R.color.white))
                        b.btnSelesaikan.visibility = View.GONE
                    }
                }

                // Item pesanan dengan collapse (max 3)
                val allDetails = pesanan.details ?: emptyList()
                var isExpanded = false

                fun renderItems(expanded: Boolean) {
                    val shown = if (expanded) allDetails else allDetails.take(MAX_ITEM_SHOWN)
                    b.rvItemPesanan.layoutManager = LinearLayoutManager(context)
                    b.rvItemPesanan.adapter = ItemPesananMiniAdapter(shown)
                    b.rvItemPesanan.isNestedScrollingEnabled = false

                    if (allDetails.size > MAX_ITEM_SHOWN) {
                        b.btnLihatSemuaItem.visibility = View.VISIBLE
                        b.btnLihatSemuaItem.text = if (expanded)
                            "▲  Sembunyikan"
                        else
                            "▼  Lihat semua pesanan (${allDetails.size})"
                    } else {
                        b.btnLihatSemuaItem.visibility = View.GONE
                    }
                }

                renderItems(false)

                b.btnLihatSemuaItem.setOnClickListener {
                    isExpanded = !isExpanded
                    renderItems(isExpanded)
                }

                b.tvJarak.text = pesanan.jarak ?: "- m"
                b.tvDurasi.text = pesanan.durasi ?: "- menit"

                b.tvOngkir.text = nf.format(pesanan.ongkir ?: 0)
                b.tvBiayaLayanan.text = nf.format(pesanan.biayaLayanan ?: 0)

                // Total
                b.tvTotal.text = nf.format(pesanan.totalHarga)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}