package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.pengaturan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentPanduanPenggunaBinding

class PanduanPenggunaFragment : Fragment() {

    private var _binding: FragmentPanduanPenggunaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPanduanPenggunaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val listPanduan = listOf(
            PanduanModel("1", "Login ke aplikasi", "Masukkan email & password yang sudah terdaftar."),
            PanduanModel("2", "Melakukan absensi", "Lakukan absensi di beranda untuk memulai order."),
            PanduanModel("3", "Terima pesanan", "Tap \"Terima\" saat ada pesanan masuk sebelum di ambil oleh driver lain."),
            PanduanModel("4", "Antar ke tujuan", "Ikuti rute dan konfirmasi setelah pesanan diterima pelanggan.")
        )

        binding.rvPanduan.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PanduanAdapter(listPanduan)
            setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class PanduanModel(val nomor: String, val judul: String, val deskripsi: String)
}