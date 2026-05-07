package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemDetailProdukBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan

class ItemDetailAdapter(private val items: List<Pesanan.ItemPesanan>) :
    RecyclerView.Adapter<ItemDetailAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDetailProdukBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetailProdukBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
        nf.maximumFractionDigits = 0

        holder.binding.tvNamaBarang.text = item.namaProduk
        holder.binding.tvQty.text = "${item.qty}x"
        holder.binding.tvTotal.text = nf.format(item.subtotal ?: 0)
    }

    override fun getItemCount() = items.size
}