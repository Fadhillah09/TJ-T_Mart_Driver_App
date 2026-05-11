package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.riwayat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentRiwayatBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.MessageResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatFragment : Fragment() {

    private var _binding: FragmentRiwayatBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    private var fullList: List<Pesanan> = emptyList()
    private var activeTab = "semua" // semua, selesai, belum_selesai

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        binding.rvRiwayatFull.layoutManager = LinearLayoutManager(requireContext())

        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_red_dark)
        binding.swipeRefresh.setOnRefreshListener { loadRiwayatData() }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupTabs()
        setupSearch()
        loadRiwayatData()
    }

    private fun setupTabs() {
        binding.tabSemua.setOnClickListener { setTab("semua") }
        binding.tabSelesai.setOnClickListener { setTab("selesai") }
        binding.tabBelumSelesai.setOnClickListener { setTab("belum_selesai") }
    }

    private fun setTab(tab: String) {
        activeTab = tab
        listOf(binding.tabSemua, binding.tabSelesai, binding.tabBelumSelesai).forEach {
            it.setBackgroundResource(R.drawable.bg_tab_unselected)
            it.setTextColor(requireContext().getColor(R.color.text_sub))
        }

        // Set tab aktif
        val selectedTab = when (tab) {
            "selesai" -> binding.tabSelesai
            "belum_selesai" -> binding.tabBelumSelesai
            else -> binding.tabSemua
        }
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected)
        selectedTab.setTextColor(requireContext().getColor(R.color.white))

        applyFilterAndSearch()
    }

    private fun setupSearch() {
        binding.etCari.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilterAndSearch() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun applyFilterAndSearch() {
        val query = binding.etCari.text.toString().lowercase().trim()

        val filtered = fullList.filter { pesanan ->
            // Filter tab
            val statusMatch = when (activeTab) {
                "selesai" -> pesanan.statusAntar?.lowercase() in listOf("selesai", "tiba")
                "belum_selesai" -> pesanan.statusAntar?.lowercase() in listOf("sedang diantar", "dibatalkan")
                else -> true
            }

            // Filter search
            val searchMatch = if (query.isEmpty()) true else {
                val nama = pesanan.user?.name?.lowercase() ?: ""
                val alamat = pesanan.alamatDisplay?.lowercase() ?: ""
                nama.contains(query) || alamat.contains(query)
            }

            statusMatch && searchMatch
        }

        // Group by tanggal
        tampilkanDenganTanggal(filtered)
    }

    private fun tampilkanDenganTanggal(list: List<Pesanan>) {
        if (_binding == null) return
        binding.rvRiwayatFull.adapter = RiwayatFullAdapter(list) { id ->
            aksiSelesaikanPesanan(id)
        }
    }

    private fun loadRiwayatData() {
        binding.swipeRefresh.isRefreshing = true
        ApiClient.instance.getRiwayat(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(call: Call<PesananResponse>, response: Response<PesananResponse>) {
                    if (_binding == null) return
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body() != null) {
                        fullList = response.body()?.data ?: emptyList()

                        val raw = fullList.firstOrNull()

                        applyFilterAndSearch()
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(context, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun aksiSelesaikanPesanan(id: Int) {
        ApiClient.instance.selesaikanPesanan(session.getBearerToken(), id)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Pesanan Berhasil Diselesaikan!", Toast.LENGTH_SHORT).show()
                        loadRiwayatData()
                    }
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {}
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}