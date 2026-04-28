package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.MessageResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.OmsetResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentBerandaBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.NotifikasiFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.profil.ProfilFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager
    private var pesananAktif: Pesanan? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        binding.tvNamaDriver.text = "Halo ${session.getUserName()}!"
        binding.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRiwayat.isNestedScrollingEnabled = false

        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_red_dark)
        binding.swipeRefresh.setOnRefreshListener { loadAllData() }

        binding.btnTolak.setOnClickListener {
            pesananAktif = null
            tampilKosong()
        }

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
                        val list = response.body()?.data

                        val belumDiklaim = list?.find { it.kurirId == null }
                        val sudahDiklaim = list?.find {
                            it.kurirId == session.getUserId() && it.statusAntar == "sedang diantar"
                        }

                        val tampil = belumDiklaim ?: sudahDiklaim

                        if (tampil != null) {
                            pesananAktif = tampil
                            tampilPesanan(tampil)
                        } else {
                            tampilKosong()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal load pesanan: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun tampilPesanan(p: Pesanan) {
        binding.layoutPesananAda.visibility = View.VISIBLE
        binding.layoutPesananKosong.visibility = View.GONE

        binding.tvNamaPemesan.text = p.user?.name ?: "Customer"
        binding.tvLokasiPesanan.text = p.user?.getNamaLokasiLengkap() ?: "Lokasi tidak diketahui"

        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        binding.tvTotalHarga.text = nf.format(p.totalHarga)
        binding.tvStatusPesanan.text = p.statusAntar ?: "Siap Diantar"

        if (p.kurirId != null && p.kurirId == session.getUserId()) {
            binding.btnClaim.text = "Tandai Selesai"
            binding.btnClaim.setOnClickListener { selesaikanPesanan(p.id) }
            binding.btnTolak.visibility = View.GONE
        } else {
            binding.btnClaim.text = "Terima"
            binding.btnClaim.setOnClickListener { claimPesanan(p.id) }
            binding.btnTolak.visibility = View.VISIBLE
        }
    }

    private fun tampilKosong() {
        binding.layoutPesananAda.visibility = View.GONE
        binding.layoutPesananKosong.visibility = View.VISIBLE
    }

    private fun claimPesanan(id: Int) {
        ApiClient.instance.claimPesanan(session.getBearerToken(), id)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Pesanan diterima!", Toast.LENGTH_SHORT).show()
                        loadPesanan()
                    } else {
                        Toast.makeText(requireContext(), "Gagal klaim: ${response.code()} - ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
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
                        pesananAktif = null
                        loadAllData()
                    } else {
                        Toast.makeText(requireContext(), "Gagal selesaikan: ${response.code()} - ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
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
                        binding.tvSaldo.text = nf.format(data.saldo)
                        binding.tvNomorRek.text = data.nomorRekening ?: "-"
                        binding.tvNamaBank.text = data.namaBank ?: "-"
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
                        val list = response.body()?.data ?: emptyList()
                        binding.rvRiwayat.adapter = RiwayatAdapter(list)
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {}
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}