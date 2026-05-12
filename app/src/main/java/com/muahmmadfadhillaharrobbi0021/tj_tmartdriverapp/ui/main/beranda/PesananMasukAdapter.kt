package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class PesananMasukAdapter(
    private val list: List<Pesanan>,
    private val viewMode: Int = VIEW_GESER,
    private val isActive: Boolean = false,
    private val onAccept: (Pesanan) -> Unit,
    private val onReject: (Pesanan) -> Unit,
    private val onItemClick: (Pesanan) -> Unit
) : RecyclerView.Adapter<PesananMasukAdapter.ViewHolder>() {

    companion object {
        const val VIEW_GESER = 0
        const val VIEW_BAWAH = 1
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvIdPesanan)
        val tvNama: TextView = view.findViewById(R.id.tvNamaPembeli)
        val tvAlamat: TextView = view.findViewById(R.id.tvAlamat)
        val tvHarga: TextView = view.findViewById(R.id.tvTotalHarga)
        val tvMetode: TextView = view.findViewById(R.id.tvMetodePembayaran)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnTolak: Button = view.findViewById(R.id.btnTolak)
        val btnTerima: Button = view.findViewById(R.id.btnTerima)
    }

    override fun getItemViewType(position: Int): Int = viewMode

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutRes = if (viewType == VIEW_GESER) {
            R.layout.item_pesanan_geser
        } else {
            R.layout.item_pesanan_masuk
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)

        // Saat mode geser tapi pesanan aktif → card full width
        if (viewType == VIEW_GESER && isActive) {
            val params = view.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = params
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pesanan = list[position]
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        nf.maximumFractionDigits = 0

        holder.tvId.text = pesanan.namaMart
            ?: pesanan.user?.lokasi?.namaMart
                    ?: pesanan.user?.lokasi?.namaLokasi
                    ?: "-"
        holder.tvNama.text = pesanan.user?.name ?: "Pelanggan"
        holder.tvAlamat.text = pesanan.alamatDisplay ?: pesanan.user?.getNamaLokasiLengkap() ?: "-"
        holder.tvHarga.text = nf.format(pesanan.totalHarga)
        holder.tvMetode.text = "Pembayaran: ${pesanan.pembayaranDisplay ?: pesanan.metodePembayaran ?: "-"}"
        holder.tvStatus.text = pesanan.statusAntar ?: "-"

        if (isActive) {
            holder.btnTerima.text = "Pesanan Selesai"
            holder.btnTolak.text = "Batalkan"
        } else {
            holder.btnTerima.text = "Terima"
            holder.btnTolak.text = "Tolak"
        }

        holder.btnTerima.setOnClickListener { onAccept(pesanan) }
        holder.btnTolak.setOnClickListener { onReject(pesanan) }
        holder.itemView.setOnClickListener { onItemClick(pesanan) }
    }

    override fun getItemCount(): Int = list.size
}