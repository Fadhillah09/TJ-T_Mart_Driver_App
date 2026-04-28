package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import java.text.NumberFormat
import java.util.*

class RiwayatAdapter(private val list: List<Pesanan>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaPelanggan: TextView = view.findViewById(R.id.tvNamaPelanggan)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        holder.tvNamaPelanggan.text = item.user?.name ?: "User"
        holder.tvTotal.text = nf.format(item.totalHarga)

        val statusAntar = item.statusAntar ?: ""
        if (statusAntar.equals("selesai", ignoreCase = true)) {
            holder.tvStatus.text = "Selesai"
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_selesai)
        } else {
            holder.tvStatus.text = "Belum selesai"
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_belum_selesai)
        }
    }

    override fun getItemCount(): Int = list.size
}