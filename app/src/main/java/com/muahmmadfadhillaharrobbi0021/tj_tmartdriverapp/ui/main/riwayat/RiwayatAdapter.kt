package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.riwayat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemRiwayatBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class RiwayatAdapter(
    private val list: List<Pesanan>,
    private val onSelesaiClick: ((Int) -> Unit)? = null  // ← opsional, default null
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

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

            when (pesanan.statusAntar?.lowercase()) {
                "selesai", "tiba" -> {  // ← 'tiba' tampil sebagai "Selesai"
                    tvStatus.text = "Selesai"
                    tvStatus.setTextColor(context.getColor(android.R.color.darker_gray))
                    btnSelesaikan.visibility = View.GONE
                }
                "dibatalkan" -> {
                    tvStatus.text = "Dibatalkan"
                    tvStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                    btnSelesaikan.visibility = View.GONE
                }
                "sedang diantar" -> {
                    tvStatus.text = "Belum selesai"
                    tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                    if (onSelesaiClick != null) {
                        btnSelesaikan.visibility = View.VISIBLE
                        btnSelesaikan.setOnClickListener { onSelesaiClick.invoke(pesanan.id) }
                    } else {
                        btnSelesaikan.visibility = View.GONE
                    }
                }
                else -> {
                    tvStatus.text = "Selesai"
                    tvStatus.setTextColor(context.getColor(android.R.color.darker_gray))
                    btnSelesaikan.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size
}