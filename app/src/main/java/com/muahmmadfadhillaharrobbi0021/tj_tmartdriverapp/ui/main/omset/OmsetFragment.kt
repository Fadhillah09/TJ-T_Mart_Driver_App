package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.omset

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentOmsetBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.GrafikResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.OmsetResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.RewardResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class OmsetFragment : Fragment() {

    private var _binding: FragmentOmsetBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    // sensor/unsensor saldo
    private var isSaldoVisible = true
    private var currentSaldoFormatted = ""

    // filter grafik
    private var filterAktif = "bulan"

    // riwayat show/hide
    private var fullRiwayatList: List<Pesanan> = emptyList()
    private var isShowingAll = false
    private val LIMIT = 6

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOmsetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        // tombol back → navigasi ke tab beranda via bottomNavigation
        binding.btnBack.setOnClickListener {
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            )?.selectedItemId = R.id.nav_beranda
        }

        binding.rvRiwayatOmset.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRiwayatOmset.isNestedScrollingEnabled = false

        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_red_dark)
        binding.swipeRefresh.setOnRefreshListener { loadAllData() }

        // toggle sensor/unsensor saldo
        binding.ivToggleSaldo.setOnClickListener {
            isSaldoVisible = !isSaldoVisible
            updateSaldoVisibility()
        }

        // tombol filter grafik
        binding.btnFilterMinggu.setOnClickListener {
            filterAktif = "minggu"
            updateFilterUI()
            loadGrafik()
        }
        binding.btnFilterBulan.setOnClickListener {
            filterAktif = "bulan"
            updateFilterUI()
            loadGrafik()
        }
        updateFilterUI()

        // tombol lihat semua / lihat lebih sedikit
        binding.btnLihatSemua.setOnClickListener {
            isShowingAll = !isShowingAll
            updateRiwayatList()
        }

        // tombol klaim reward
        binding.btnKlaimReward.setOnClickListener {
            klaimReward()
        }

        loadAllData()
    }

    private fun loadAllData() {
        loadOmset()
        loadRiwayat()
        loadGrafik()
        loadRewardStatus()
    }

    private fun updateSaldoVisibility() {
        if (_binding == null) return
        if (isSaldoVisible) {
            binding.tvSaldo.text = currentSaldoFormatted
            binding.ivToggleSaldo.setImageResource(R.drawable.ic_eye_open)
        } else {
            binding.tvSaldo.text = "••••••"
            binding.ivToggleSaldo.setImageResource(R.drawable.ic_eye_closed)
        }
    }

    private fun loadOmset() {
        ApiClient.instance.getOmset(session.getBearerToken())
            .enqueue(object : Callback<OmsetResponse> {
                override fun onResponse(call: Call<OmsetResponse>, response: Response<OmsetResponse>) {
                    if (_binding == null) return
                    binding.swipeRefresh.isRefreshing = false
                    if (response.isSuccessful && response.body()?.data != null) {
                        val data = response.body()!!.data!!
                        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        nf.maximumFractionDigits = 0
                        currentSaldoFormatted = nf.format(data.saldo)
                        updateSaldoVisibility()
                        binding.tvNomorRek.text = data.nomorRekening ?: "-"
                        binding.tvTanggalGaji.text = data.tanggalGaji ?: "-"
                    }
                }
                override fun onFailure(call: Call<OmsetResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.swipeRefresh.isRefreshing = false
                }
            })
    }

    private fun loadRiwayat() {
        ApiClient.instance.getRiwayat(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(call: Call<PesananResponse>, response: Response<PesananResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()?.data ?: emptyList()
                        if (list.isEmpty()) {
                            binding.tvRiwayatKosong.visibility = View.VISIBLE
                            binding.rvRiwayatOmset.visibility = View.GONE
                            binding.btnLihatSemua.visibility = View.GONE
                        } else {
                            binding.tvRiwayatKosong.visibility = View.GONE
                            binding.rvRiwayatOmset.visibility = View.VISIBLE
                            fullRiwayatList = list
                            isShowingAll = false
                            updateRiwayatList()
                        }
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    if (_binding == null) return
                }
            })
    }

    private fun updateRiwayatList() {
        if (_binding == null) return
        val tampil = if (isShowingAll) fullRiwayatList else fullRiwayatList.take(LIMIT)
        binding.rvRiwayatOmset.adapter = RiwayatOmsetAdapter(tampil)

        if (fullRiwayatList.size > LIMIT) {
            binding.btnLihatSemua.visibility = View.VISIBLE
            binding.btnLihatSemua.text = if (isShowingAll) "Lihat lebih sedikit" else "Lihat semua"
        } else {
            binding.btnLihatSemua.visibility = View.GONE
        }
    }

    private fun loadGrafik() {
        if (_binding == null) return
        ApiClient.instance.getGrafik(session.getBearerToken(), filterAktif)
            .enqueue(object : Callback<GrafikResponse> {
                override fun onResponse(call: Call<GrafikResponse>, response: Response<GrafikResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful && response.body()?.data != null) {
                        setupGrafik(response.body()!!.data!!)
                    }
                }
                override fun onFailure(call: Call<GrafikResponse>, t: Throwable) {
                    if (_binding == null) return
                }
            })
    }

    private fun loadRewardStatus() {
        if (_binding == null) return
        ApiClient.instance.getRewardStatus(session.getBearerToken())
            .enqueue(object : Callback<RewardResponse> {
                override fun onResponse(call: Call<RewardResponse>, response: Response<RewardResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful && response.body()?.data != null) {
                        updateRewardUI(response.body()!!.data!!)
                    }
                }
                override fun onFailure(call: Call<RewardResponse>, t: Throwable) {
                    if (_binding == null) return
                }
            })
    }

    private fun updateRewardUI(data: RewardResponse.RewardData) {
        if (_binding == null) return
        val pesanan = data.pesananBulanIni ?: 0
        val target = data.target ?: 10
        val sudahKlaim = data.sudahKlaim ?: false
        val bisaKlaim = data.bisaKlaim ?: false

        binding.tvRewardProgress.text = "$pesanan / $target pesanan"
        val persen = ((pesanan.toFloat() / target) * 100).toInt().coerceAtMost(100)
        binding.tvRewardPersen.text = "$persen%"
        binding.progressReward.max = target
        binding.progressReward.progress = pesanan.coerceAtMost(target)

        when {
            sudahKlaim -> {
                binding.btnKlaimReward.isEnabled = false
                binding.btnKlaimReward.text = "Reward Sudah Diklaim"
                binding.btnKlaimReward.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#888888"))
            }
            bisaKlaim -> {
                binding.btnKlaimReward.isEnabled = true
                binding.btnKlaimReward.text = "Klaim Reward Rp300.000"
                binding.btnKlaimReward.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#A8000E"))
            }
            else -> {
                binding.btnKlaimReward.isEnabled = false
                binding.btnKlaimReward.text = "Klaim Reward (${target - pesanan} pesanan lagi)"
                binding.btnKlaimReward.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#CCCCCC"))
            }
        }
    }

    private fun klaimReward() {
        binding.btnKlaimReward.isEnabled = false
        ApiClient.instance.klaimReward(session.getBearerToken())
            .enqueue(object : Callback<RewardResponse> {
                override fun onResponse(call: Call<RewardResponse>, response: Response<RewardResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Reward Rp300.000 berhasil diklaim!", Toast.LENGTH_LONG).show()
                        loadAllData()
                    } else {
                        Toast.makeText(requireContext(), "Gagal klaim reward.", Toast.LENGTH_SHORT).show()
                        binding.btnKlaimReward.isEnabled = true
                    }
                }
                override fun onFailure(call: Call<RewardResponse>, t: Throwable) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    binding.btnKlaimReward.isEnabled = true
                }
            })
    }

    private fun setupGrafik(items: List<GrafikResponse.GrafikItem>) {
        if (_binding == null) return
        val entries = items.mapIndexed { i, item -> BarEntry(i.toFloat(), item.total.toFloat()) }
        val labels = items.map { it.label ?: "" }

        val dataSet = BarDataSet(entries, "").apply {
            color = Color.parseColor("#CC2B2B")
            valueTextColor = Color.parseColor("#333333")
            valueTextSize = 9f
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.5f }
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            setDrawGridBackground(false)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#888888")
                textSize = 11f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F0F0")
                textColor = Color.parseColor("#888888")
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            animateY(500)
            invalidate()
        }
    }

    private fun updateFilterUI() {
        if (_binding == null) return
        val aktifBg = Color.parseColor("#CC2B2B")
        val nonaktifBg = Color.parseColor("#F0F0F0")
        val aktifText = Color.WHITE
        val nonaktifText = Color.parseColor("#333333")

        if (filterAktif == "minggu") {
            binding.btnFilterMinggu.setBackgroundColor(aktifBg)
            binding.btnFilterMinggu.setTextColor(aktifText)
            binding.btnFilterBulan.setBackgroundColor(nonaktifBg)
            binding.btnFilterBulan.setTextColor(nonaktifText)
        } else {
            binding.btnFilterBulan.setBackgroundColor(aktifBg)
            binding.btnFilterBulan.setTextColor(aktifText)
            binding.btnFilterMinggu.setBackgroundColor(nonaktifBg)
            binding.btnFilterMinggu.setTextColor(nonaktifText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}