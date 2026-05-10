package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R

class NotifikasiAdapter(
    private val items: MutableList<NotifikasiItem>,
    private val onItemClick: (NotifikasiItem) -> Unit
) : RecyclerView.Adapter<NotifikasiAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val viewDot: View = view.findViewById(R.id.viewDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notifikasi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvMessage.text = item.message
        holder.tvTime.text = item.time

        // Tampilkan dot merah/hijau jika belum dibaca
        holder.viewDot.visibility = if (item.isRead) View.INVISIBLE else View.VISIBLE

        // Warna background item: hijau muda jika belum dibaca, putih jika sudah
        val bgRes = if (!item.isRead) R.drawable.bg_notif_item_unread else R.drawable.bg_notif_item
        holder.itemView.setBackgroundResource(bgRes)

        // Icon sesuai tipe notifikasi
        val iconRes = when (item.type) {
            NotifType.PESANAN -> R.drawable.outline_shopping_basket_24 // ganti sesuai drawable yang ada
            NotifType.INFO -> android.R.drawable.ic_dialog_info
        }
        holder.ivIcon.setImageResource(iconRes)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NotifikasiItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}