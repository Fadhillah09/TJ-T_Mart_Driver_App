package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.riwayat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemPesananMiniBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class ItemPesananMiniAdapter(
    private val list: List<Pesanan.ItemPesanan>
) : RecyclerView.Adapter<ItemPesananMiniAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPesananMiniBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPesananMiniBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        nf.maximumFractionDigits = 0

        val qty = item.qty ?: item.jumlah ?: 1
        holder.binding.tvNamaProduk.text = "x$qty ${item.namaProduk ?: "-"}"
        holder.binding.tvHargaProduk.text = nf.format(item.subtotal ?: 0)
    }

    override fun getItemCount(): Int = list.size
}