package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemDetailProdukBinding  // ← fix
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.Constants
import java.text.NumberFormat
import java.util.Locale

class ItemDetailAdapter(private val items: List<Pesanan.ItemPesanan>) :
    RecyclerView.Adapter<ItemDetailAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDetailProdukBinding) :  // ← fix
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetailProdukBinding.inflate(  // ← fix
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

        with(holder.binding) {
            tvNamaBarang.text  = item.namaProduk ?: "-"
            tvHargaSatuan.text = nf.format(item.harga ?: 0)
            val qty = item.qty ?: item.jumlah ?: 0
            tvQty.text         = "Quantity: $qty"
            tvTotal.text       = nf.format(item.subtotal ?: 0)

            val fotoUrl = item.fotoProduk
            if (!fotoUrl.isNullOrBlank()) {
                Glide.with(ivFotoProduk.context)
                    .load("${Constants.BASE_URL}storage/$fotoUrl")
                    .placeholder(R.drawable.ic_placeholder_product)
                    .error(R.drawable.ic_placeholder_product)
                    .centerCrop()
                    .into(ivFotoProduk)
            } else {
                ivFotoProduk.setImageResource(R.drawable.ic_placeholder_product)
            }
        }
    }

    override fun getItemCount() = items.size
}