package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentNotifikasiBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda.DetailPesananFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager

class NotifikasiFragment : Fragment() {

    private var _binding: FragmentNotifikasiBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NotifikasiAdapter
    private lateinit var session: SessionManager
    private var activeFilter = FilterType.SEMUA

    enum class FilterType { SEMUA, TERBARU, BELUM_DIBACA }

    companion object {
        val allNotifikasi = mutableListOf<NotifikasiItem>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())

        // Load notifikasi dari storage saat pertama buka, urutkan dari terbaru
        if (allNotifikasi.isEmpty()) {
            val saved = NotifikasiStorage.load(requireContext())
            val sorted = saved.sortedByDescending { it.timestamp }
            allNotifikasi.addAll(sorted)
        }

        setupRecyclerView()
        setupFilterChips()
        setupBacaSemua()
        updateUnreadCount()
        applyFilter(FilterType.SEMUA)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotifikasiAdapter(
            items = mutableListOf(),
            onItemClick = { item ->
                markAsRead(item)
                item.pesananId?.let { pesananId ->
                    val detailFragment = DetailPesananFragment.newInstance(pesananId)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack("DetailPesanan")
                        .commit()
                }
            }
        )
        binding.rvNotifikasi.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifikasi.adapter = adapter
    }

    private fun setupFilterChips() {
        binding.chipSemua.setOnClickListener {
            setActiveChip(FilterType.SEMUA)
            applyFilter(FilterType.SEMUA)
        }
        binding.chipTerbaru.setOnClickListener {
            setActiveChip(FilterType.TERBARU)
            applyFilter(FilterType.TERBARU)
        }
        binding.chipBelumDibaca.setOnClickListener {
            setActiveChip(FilterType.BELUM_DIBACA)
            applyFilter(FilterType.BELUM_DIBACA)
        }
    }

    private fun setupBacaSemua() {
        binding.btnBacaSemua.setOnClickListener {
            allNotifikasi.forEach { it.isRead = true }
            NotifikasiStorage.save(requireContext(), allNotifikasi)
            updateUnreadCount()
            applyFilter(activeFilter)
        }
    }

    private fun setActiveChip(filter: FilterType) {
        activeFilter = filter

        listOf(binding.chipSemua, binding.chipTerbaru, binding.chipBelumDibaca).forEach { chip ->
            chip.setBackgroundResource(R.drawable.bg_chip_inactive)
            chip.setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }

        val activeChip: TextView = when (filter) {
            FilterType.SEMUA -> binding.chipSemua
            FilterType.TERBARU -> binding.chipTerbaru
            FilterType.BELUM_DIBACA -> binding.chipBelumDibaca
        }
        activeChip.setBackgroundResource(R.drawable.bg_chip_active)
        activeChip.setTextColor(resources.getColor(android.R.color.white, null))
    }

    private fun applyFilter(filter: FilterType) {
        val filtered = when (filter) {
            FilterType.SEMUA -> allNotifikasi.sortedByDescending { it.timestamp }
            FilterType.TERBARU -> allNotifikasi.sortedByDescending { it.timestamp }
            FilterType.BELUM_DIBACA -> allNotifikasi.filter { !it.isRead }.sortedByDescending { it.timestamp }
        }

        adapter.updateData(filtered)
        binding.rvNotifikasi.scrollToPosition(0)

        if (filtered.isEmpty()) {
            binding.rvNotifikasi.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvNotifikasi.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun markAsRead(item: NotifikasiItem) {
        val index = allNotifikasi.indexOfFirst { it.id == item.id }
        if (index != -1) {
            allNotifikasi[index].isRead = true
            NotifikasiStorage.save(requireContext(), allNotifikasi)
            updateUnreadCount()
            applyFilter(activeFilter)
        }
    }

    private fun updateUnreadCount() {
        val unread = allNotifikasi.count { !it.isRead }
        binding.tvUnreadCount.text = if (unread > 0) {
            "Kamu memiliki $unread notifikasi yang belum dibaca"
        } else {
            "Semua notifikasi telah dibaca"
        }
    }

    override fun onResume() {
        super.onResume()
        updateUnreadCount()
        applyFilter(activeFilter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}