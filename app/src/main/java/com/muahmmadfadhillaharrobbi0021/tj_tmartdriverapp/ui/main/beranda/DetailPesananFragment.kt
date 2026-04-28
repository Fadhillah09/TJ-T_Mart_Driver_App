package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentDetailPesananBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
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

        // Load data detail dari API
        fetchDetailPesanan()

        binding.btnDiantar.setOnClickListener {
            // Logika ketika tombol diantar ditekan
            Toast.makeText(requireContext(), "Pesanan sedang dalam pengantaran", Toast.LENGTH_SHORT).show()
            // Di sini kamu bisa arahkan ke fragment berikutnya jika ada
        }
    }

    private fun fetchDetailPesanan() {
        ApiClient.instance.getPesanan(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(call: Call<PesananResponse>, response: Response<PesananResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        val allData = response.body()?.data ?: emptyList()
                        // Cari pesanan yang spesifik berdasarkan ID
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

        with(binding) {
            tvNamaPembeli.text = pesanan.user?.name ?: "Pelanggan"
            tvAlamatGedung.text = pesanan.user?.getNamaLokasiLengkap() ?: "Alamat tidak tersedia"
            tvMetodePembayaran.text = "Metode: ${pesanan.metodePembayaran ?: "Cash"}"
            tvTotalHarga.text = "Total: ${nf.format(pesanan.totalHarga ?: 0)}"

            // Setup RecyclerView untuk Item yang dibeli
            pesanan.details?.let { items ->
                rvItemPesanan.layoutManager = LinearLayoutManager(requireContext())
                rvItemPesanan.adapter = ItemDetailAdapter(items)
            }

            // Logika Klik Maps berdasarkan koordinat asrama
            btnOpenMaps.setOnClickListener {
                openGoogleMaps(pesanan.user?.alamatGedung ?: "")
            }
        }
    }

    private fun openGoogleMaps(gedungName: String) {
        // Data koordinat sesuai dengan informasi yang kamu berikan
        val coords = when (gedungName) {
            "Gedung 1" -> "-6.9710403,107.6283141"
            "Gedung 2" -> "-6.9707509,107.6283404"
            "Gedung 3" -> "-6.9704344,107.6283533"
            "Gedung 4" -> "-6.9709904,107.6277174"
            "Gedung 5" -> "-6.9706729,107.627767"
            "Gedung 6" -> "-6.970935,107.6271111"
            "Gedung 7" -> "-6.9706223,107.6271815"
            "Gedung 8" -> "-6.9702831,107.6272323"
            "Gedung 9" -> "-6.9700347,107.6277742"
            "Gedung 10" -> "-6.9697409,107.6278167"
            "Gedung 11" -> "-6.9700978,107.6283584"
            "Gedung 12" -> "-6.9697555,107.6283976"
            "Gedung A" -> "-6.9740468,107.6285963"
            "Gedung B" -> "-6.9736757,107.6286558"
            "Gedung C" -> "-6.9732535,107.6287044"
            "Gedung D" -> "-6.9728527,107.6286204"
            "Gedung E" -> "-6.9725544,107.6286242"
            "Gedung F" -> "-6.9720839,107.6286579"
            else -> "-6.9706729,107.627767" // Default Gedung 5
        }

        // Gunakan mode navigasi agar kurir langsung diarahkan jalannya
        val gmmIntentUri = Uri.parse("google.navigation:q=$coords")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Jika tidak ada app Maps, buka browser
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$coords")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}