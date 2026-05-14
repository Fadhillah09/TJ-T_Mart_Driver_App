package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.omset

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ItemRiwayatOmsetBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatOmsetAdapter(private val list: List<Pesanan>) :
    RecyclerView.Adapter<RiwayatOmsetAdapter.VH>() {

    inner class VH(val binding: ItemRiwayatOmsetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRiwayatOmsetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        val b = holder.binding

        val isReward = item.idTransaksi?.startsWith("REWARD-") == true

        if (isReward) {
            // Tampilan khusus row bonus reward
            b.ivIcon.setImageResource(R.drawable.ic_nav_omset)
            b.ivIcon.setColorFilter(Color.parseColor("#5DAA64"))
            b.tvNamaPelanggan.text = "Bonus Reward Bulanan"
            b.tvRute.text = "Target 150 pesanan tercapai"
            b.tvRute.setTextColor(Color.parseColor("#5DAA64"))
        } else {
            // Tampilan normal pesanan biasa
            b.ivIcon.setImageResource(R.drawable.ic_motor)
            b.ivIcon.clearColorFilter()
            b.tvNamaPelanggan.text = item.user?.name ?: "-"
            val lokasiPelanggan = item.alamatDisplay ?: item.user?.getNamaLokasiLengkap() ?: "-"
            b.tvRute.text = lokasiPelanggan
            b.tvRute.setTextColor(Color.parseColor("#888888"))
        }

        // Tanggal dari updatedAt (sama untuk semua row)
        val tanggal = try {
            val formatList = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
            )
            val sdfOut = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id"))
            var parsed: String? = null
            for (fmt in formatList) {
                try {
                    val sdfIn = SimpleDateFormat(fmt, Locale.getDefault())
                    val date = sdfIn.parse(item.updatedAt ?: "")
                    if (date != null) {
                        parsed = sdfOut.format(date)
                        break
                    }
                } catch (_: Exception) {}
            }
            parsed ?: item.updatedAt?.take(10) ?: "-"
        } catch (e: Exception) {
            item.updatedAt?.take(10) ?: "-"
        }
        b.tvTanggal.text = tanggal

        // Nominal pendapatan
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        nf.maximumFractionDigits = 0
        if (isReward) {
            b.tvNominal.text = "+${nf.format(300000)}"
        } else {
            b.tvNominal.text = "+${nf.format(3000)}"
        }
        b.tvNominal.setTextColor(Color.parseColor("#5DAA64"))
    }
}