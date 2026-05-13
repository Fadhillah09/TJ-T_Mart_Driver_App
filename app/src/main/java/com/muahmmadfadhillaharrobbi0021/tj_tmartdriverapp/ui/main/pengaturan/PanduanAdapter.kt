package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.pengaturan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
// PASTIKAN: Tidak boleh ada import java.io.File di sini!
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemPanduanBinding

class PanduanAdapter(private val list: List<PanduanPenggunaFragment.PanduanModel>) :
    RecyclerView.Adapter<PanduanAdapter.ViewHolder>() {

    // ViewHolder menggunakan ItemPanduanBinding secara eksplisit
    class ViewHolder(val binding: ItemPanduanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPanduanBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // Mengisi data ke dalam View
        holder.binding.tvNomor.text = item.nomor
        holder.binding.tvJudul.text = item.judul
        holder.binding.tvDeskripsi.text = item.deskripsi
    }

    override fun getItemCount(): Int = list.size
}