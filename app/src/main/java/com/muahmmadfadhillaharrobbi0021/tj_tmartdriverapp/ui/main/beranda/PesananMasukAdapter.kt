package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemPesananMasukBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class PesananMasukAdapter(
    private val list: List<Pesanan>,
    private val onAccept: (Pesanan) -> Unit,
    private val onReject: (Pesanan) -> Unit,
    private val onItemClick: (Pesanan) -> Unit
) : RecyclerView.Adapter<PesananMasukAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPesananMasukBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPesananMasukBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        nf.maximumFractionDigits = 0

        with(holder.binding) {
            // Hilangkan tanda #
            tvIdPesanan.text = item.id.toString()
            tvNamaPembeli.text = item.user?.name ?: "Pelanggan"
            tvAlamat.text = item.user?.getNamaLokasiLengkap() ?: "Alamat tidak tersedia"
            tvTotalHarga.text = nf.format(item.totalHarga ?: 0)

            // Tampilkan Metode Pembayaran (asumsi field: metodePembayaran)
            tvMetodePembayaran.text = "Pembayaran: ${item.metodePembayaran ?: "Cash"}"

            tvStatus.text = item.statusAntar?.replaceFirstChar { it.uppercase() }

            if (item.statusAntar?.lowercase() == "sedang diantar") {
                btnTerima.text = "Selesaikan"
                btnTolak.isEnabled = false
                btnTolak.alpha = 0.5f
            } else {
                btnTerima.text = "Terima"
                btnTolak.isEnabled = true
                btnTolak.alpha = 1.0f
            }

            btnTerima.setOnClickListener { onAccept(item) }
            btnTolak.setOnClickListener { onReject(item) }

            // Klik pada kartu untuk melihat detail
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size
}