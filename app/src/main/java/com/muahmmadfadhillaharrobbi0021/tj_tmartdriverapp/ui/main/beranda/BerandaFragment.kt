package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentBerandaBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.MessageResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.OmsetResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.NotifikasiFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.profil.ProfilFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        binding.tvNamaDriver.text = "Halo ${session.getUserName()}!"

        // Setup RecyclerView Riwayat
        binding.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRiwayat.isNestedScrollingEnabled = false

        // Setup RecyclerView Pesanan Masuk (Antrian)
        binding.rvPesananMasuk.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPesananMasuk.isNestedScrollingEnabled = false

        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_red_dark)
        binding.swipeRefresh.setOnRefreshListener { loadAllData() }

        // Navigasi ke ProfilFragment
        binding.layoutAvatar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfilFragment())
                .addToBackStack(null)
                .commit()
        }

        // Navigasi ke NotifikasiFragment
        binding.ivNotifikasi.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NotifikasiFragment())
                .addToBackStack(null)
                .commit()
        }

        loadAllData()
    }

    private fun loadAllData() {
        loadPesanan()
        loadOmset()
        loadRiwayat()
    }

    private fun loadPesanan() {
        ApiClient.instance.getPesanan(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(call: Call<PesananResponse>, response: Response<PesananResponse>) {
                    if (_binding == null) return
                    binding.swipeRefresh.isRefreshing = false

                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()?.data ?: emptyList()
                        val myId = session.getUserId()

                        // 1. Cari pesanan yang sedang SAYA antar
                        val pesananAktif = list.find {
                            it.kurirId == myId && it.statusAntar?.lowercase() == "sedang diantar"
                        }

                        if (pesananAktif != null) {
                            // Jika ada pesanan aktif, tetap tampilkan di daftar "Pesanan Masuk"
                            // tapi kita kirim flag isActive = true agar tombolnya berubah
                            tampilkanDaftar(listOf(pesananAktif), isActive = true)
                        } else {
                            // 2. Jika tidak ada pesanan aktif, tampilkan antrian pesanan umum (kurirId NULL)
                            val antrian = list.filter {
                                it.statusAntar?.lowercase() == "diproses" && it.kurirId == null
                            }

                            if (antrian.isNotEmpty()) {
                                tampilkanDaftar(antrian, isActive = false)
                            } else {
                                tampilKosong()
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) { /* ... */ }
            })
    }

    // Fungsi pembantu agar kode lebih rapi
    private fun tampilkanDaftar(list: List<Pesanan>, isActive: Boolean) {
        binding.rvPesananMasuk.visibility = View.VISIBLE
        binding.layoutPesananKosong.visibility = View.GONE
        binding.rvPesananMasuk.adapter = PesananMasukAdapter(
            list = list,
            onAccept = { pesanan ->
                if (isActive) selesaikanPesanan(pesanan.id) else claimPesanan(pesanan.id)
            },
            onReject = { pesanan ->
                session.rejectPesananLokal(pesanan.id)
                loadPesanan()
            },
            onItemClick = { pesanan -> bukaDetailPesanan(pesanan) }
        )
    }

    private fun tampilKosong() {
        binding.rvPesananMasuk.visibility = View.GONE
        binding.layoutPesananKosong.visibility = View.VISIBLE
    }
    private fun bukaDetailPesanan(pesanan: Pesanan) {
        val detailFragment = DetailPesananFragment.newInstance(pesanan.id)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, detailFragment)
            .addToBackStack(null) // Agar tombol back berfungsi
            .commit()
    }

    private fun claimPesanan(id: Int) {
        ApiClient.instance.claimPesanan(session.getBearerToken(), id)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Pesanan diterima!", Toast.LENGTH_SHORT).show()
                        loadPesanan()
                    } else if (response.code() == 409) {
                        // Berikan pesan yang lebih manusiawi
                        Toast.makeText(requireContext(), "Yah, pesanan sudah diambil driver lain!", Toast.LENGTH_LONG).show()
                        loadPesanan()
                    } else {
                        Toast.makeText(requireContext(), "Gagal klaim: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), "Error klaim: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun selesaikanPesanan(id: Int) {
        ApiClient.instance.selesaikanPesanan(session.getBearerToken(), id)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Pesanan selesai!", Toast.LENGTH_SHORT).show()
                        loadAllData()
                    } else {
                        Toast.makeText(requireContext(), "Gagal selesaikan: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), "Error selesai: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun loadOmset() {
        ApiClient.instance.getOmset(session.getBearerToken())
            .enqueue(object : Callback<OmsetResponse> {
                override fun onResponse(call: Call<OmsetResponse>, response: Response<OmsetResponse>) {
                    if (_binding != null && response.isSuccessful && response.body()?.data != null) {
                        val data = response.body()!!.data!!
                        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        nf.maximumFractionDigits = 0
                        binding.tvSaldo.text = nf.format(data.saldo)
                        binding.tvNomorRek.text = data.nomorRekening ?: "-"
                        binding.tvTanggalGaji.text = data.tanggalGaji ?: "-"
                    }
                }
                override fun onFailure(call: Call<OmsetResponse>, t: Throwable) {}
            })
    }

    private fun loadRiwayat() {
        ApiClient.instance.getRiwayat(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(call: Call<PesananResponse>, response: Response<PesananResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful && response.body() != null) {

                        // 1. Ambil daftar ID yang ditolak lokal (agar tidak muncul di riwayat juga)
                        val rejectedLocalIds = session.getRejectedPesananIds()

                        val listRaw = response.body()?.data ?: emptyList()

                        // 2. Filter data riwayat: Buang pesanan yang ID-nya ada di daftar tolak lokal
                        val listRiwayatFiltered = listRaw.filter { it.id.toString() !in rejectedLocalIds }

                        // 3. Tampilkan hanya data yang sudah difilter
                        if (listRiwayatFiltered.isNotEmpty()) {
                            binding.rvRiwayat.adapter = RiwayatAdapter(listRiwayatFiltered)
                        } else {
                            // Opsional: Jika hasil filter kosong, kosongkan adapter
                            binding.rvRiwayat.adapter = RiwayatAdapter(emptyList())
                        }
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Gagal load riwayat: ${t.message}")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}