package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.bumptech.glide.Glide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentDetailPesananBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.Constants
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class DetailPesananFragment : Fragment() {

    private var _binding: FragmentDetailPesananBinding? = null
    private val binding get() = _binding!!
    private var pesananId: Int = 0
    private lateinit var session: SessionManager

    companion object {
        fun newInstance(id: Int) = DetailPesananFragment().apply {
            arguments = Bundle().apply { putInt("PESANAN_ID", id) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pesananId = arguments?.getInt("PESANAN_ID") ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailPesananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        fetchDetailPesanan()

        binding.btnDiantar.setOnClickListener {
            val anterFragment = AnterPesananFragment.newInstance(pesananId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, anterFragment)
                .addToBackStack("AnterPesanan")
                .commit()
        }
    }

    private fun fetchDetailPesanan() {
        ApiClient.instance.getPesanan(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(call: Call<PesananResponse>, response: Response<PesananResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        val allData = response.body()?.data ?: emptyList()
                        val detail = allData.find { it.id == pesananId }
                        detail?.let { setupUI(it) }
                    }
                }

                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Gagal memuat detail: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupUI(pesanan: Pesanan) {
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        nf.maximumFractionDigits = 0

        if (_binding == null) return

        with(binding) {
            tvNamaPembeli.text = pesanan.user?.name ?: "Pelanggan Tanpa Nama"

            val fotoUser = pesanan.user?.gambar
            if (!fotoUser.isNullOrBlank()) {
                Glide.with(this@DetailPesananFragment)
                    .load("${Constants.BASE_URL}storage/$fotoUser")
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivFotoPembeli)
            }

            val alamatFix = pesanan.alamatDisplay
                ?: pesanan.user?.getNamaLokasiLengkap()
                ?: "Alamat tidak tersedia"

            tvMetodePembayaran.text = "Metode: ${pesanan.metodePembayaran ?: "Cash"}"

            val totalHarga = pesanan.totalHarga ?: 0
            val ongkir = if (pesanan.tipeLayanan == "delivery" || pesanan.tipeLayanan == "galon") 3000 else 0
            val layanan = if (pesanan.tipeLayanan == "delivery" || pesanan.tipeLayanan == "galon") 2000 else 0
            val subtotal = totalHarga - ongkir - layanan

            tvSubtotal.text = nf.format(subtotal)
            tvOngkir.text = nf.format(ongkir)
            tvBiayaLayanan.text = nf.format(layanan)
            tvTotalHarga.text = nf.format(totalHarga)

            // ↓ Banner rute: nama mart & tujuan
            val namaMart = pesanan.namaMart ?: "TJ Mart Putra"
            val namaGedung = alamatFix.split(",").firstOrNull()?.trim() ?: alamatFix
            tvNamaMartDetail.text = namaMart
            tvTujuanDetail.text = namaGedung

            // ↓ Animasi kedip berulang pada panah rute
            startRouteAnimation()

            setupWebView(namaGedung)

            pesanan.details?.let { items ->
                if (items.isNotEmpty()) {
                    rvItemPesanan.layoutManager = LinearLayoutManager(requireContext())
                    rvItemPesanan.adapter = ItemDetailAdapter(items)
                } else {
                    Toast.makeText(requireContext(), "Item pesanan kosong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startRouteAnimation() {
        if (_binding == null) return
        val arrow = binding.ivAnimasiRute

        // Animasi translasi kiri-kanan berulang untuk efek "bergerak"
        arrow.animate()
            .translationX(8f)
            .setDuration(500)
            .withEndAction {
                if (_binding == null) return@withEndAction
                arrow.animate()
                    .translationX(-8f)
                    .setDuration(500)
                    .withEndAction {
                        if (_binding != null) startRouteAnimation()
                    }
                    .start()
            }
            .start()
    }

    private fun setupWebView(namaGedung: String) {
        val coords = getCoords(namaGedung)
        val (lat, lng) = coords.split(",")

        val html = """
        <html>
        <body style="margin:0;padding:0;">
            <iframe width="100%" height="100%" frameborder="0" style="border:0" 
                src="https://maps.google.com/maps?q=$lat,$lng&z=18&output=embed">
            </iframe>
        </body>
        </html>
        """.trimIndent()

        binding.webViewMap.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        binding.webViewMap.loadDataWithBaseURL(
            "https://www.google.com", html, "text/html", "UTF-8", null
        )
    }

    private fun getCoords(gedungName: String): String = when {
        gedungName.contains("Gedung 1",  true) -> "-6.9710403,107.6283141"
        gedungName.contains("Gedung 2",  true) -> "-6.9707509,107.6283404"
        gedungName.contains("Gedung 3",  true) -> "-6.9704344,107.6283533"
        gedungName.contains("Gedung 4",  true) -> "-6.9709904,107.6277174"
        gedungName.contains("Gedung 5",  true) -> "-6.9706729,107.627767"
        gedungName.contains("Gedung 6",  true) -> "-6.970935,107.6271111"
        gedungName.contains("Gedung 7",  true) -> "-6.9706223,107.6271815"
        gedungName.contains("Gedung 8",  true) -> "-6.9702831,107.6272323"
        gedungName.contains("Gedung 9",  true) -> "-6.9700347,107.6277742"
        gedungName.contains("Gedung 10", true) -> "-6.9697409,107.6278167"
        gedungName.contains("Gedung 11", true) -> "-6.9700978,107.6283584"
        gedungName.contains("Gedung 12", true) -> "-6.9697555,107.6283976"
        gedungName.contains("Gedung A",  true) -> "-6.9740468,107.6285963"
        gedungName.contains("Gedung B",  true) -> "-6.9736757,107.6286558"
        gedungName.contains("Gedung C",  true) -> "-6.9732535,107.6287044"
        gedungName.contains("Gedung D",  true) -> "-6.9728527,107.6286204"
        gedungName.contains("Gedung E",  true) -> "-6.9725544,107.6286242"
        gedungName.contains("Gedung F",  true) -> "-6.9720839,107.6286579"
        else                                    -> "-6.9706729,107.627767"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.ivAnimasiRute.animate().cancel()
        _binding = null
    }
}