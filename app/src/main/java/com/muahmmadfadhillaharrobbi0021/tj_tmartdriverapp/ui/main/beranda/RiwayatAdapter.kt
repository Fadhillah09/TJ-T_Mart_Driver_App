package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemRiwayatBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class RiwayatAdapter(private val list: List<Pesanan>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRiwayatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pesanan = list[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvNamaPelanggan.text = pesanan.user?.name ?: "Pelanggan"

            val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            nf.maximumFractionDigits = 0
            tvTotalHarga.text = nf.format(pesanan.totalHarga)

            btnSelesaikan.visibility = android.view.View.GONE

            when (pesanan.statusAntar?.lowercase()) {
                "selesai" -> {
                    tvStatus.text = "Selesai"
                    tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_selesai)
                }
                "dibatalkan" -> {
                    tvStatus.text = "Dibatalkan"
                    tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_dibatalkan)
                }
                else -> {
                    tvStatus.text = pesanan.statusAntar ?: "-"
                    tvStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_belum_selesai)
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size
}