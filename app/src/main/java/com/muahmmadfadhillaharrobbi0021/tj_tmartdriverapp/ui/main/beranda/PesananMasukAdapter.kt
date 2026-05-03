package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.view.LayoutInflater
import android.view.View
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
            tvIdPesanan.text = item.id.toString()
            tvNamaPembeli.text = item.user?.name ?: "Pelanggan"
            tvAlamat.text = item.user?.getNamaLokasiLengkap() ?: "Alamat tidak tersedia"
            tvTotalHarga.text = nf.format(item.totalHarga ?: 0)
            tvMetodePembayaran.text = "Pembayaran: ${item.metodePembayaran ?: "Cash"}"
            tvStatus.text = item.statusAntar?.replaceFirstChar { it.uppercase() }

            // LOGIKA TOMBOL: Sesuai instruksi agar tidak hilang saat diterima
            if (item.statusAntar?.lowercase() == "sedang diantar") {
                // Jika sudah diterima, pilihan berubah menjadi Selesaikan dan Batalkan
                btnTerima.text = "Selesaikan"
                btnTolak.text = "Batalkan Pesanan"

                // Pastikan tombol batalkan aktif
                btnTolak.isEnabled = true
                btnTolak.alpha = 1.0f
            } else {
                // Jika masih dalam antrean (status diproses)
                btnTerima.text = "Terima"
                btnTolak.text = "Tolak"
                btnTolak.isEnabled = true
                btnTolak.alpha = 1.0f
            }

            // Aksi tombol
            btnTerima.setOnClickListener { onAccept(item) }
            btnTolak.setOnClickListener { onReject(item) }

            // Navigasi ke detail saat kartu diklik
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = list.size
}