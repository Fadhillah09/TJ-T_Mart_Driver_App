package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentBerandaBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.MessageResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.OmsetResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.NotifikasiFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.profil.ProfilFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    private var isViewGeser = true
    private var currentPesananList: List<Pesanan> = emptyList()
    private var currentIsActive: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        binding.tvNamaDriver.text = "Halo ${session.getUserName()}!"

        loadFotoProfil()

        binding.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRiwayat.isNestedScrollingEnabled = false

        applyLayoutManager()

        binding.swipeRefresh.setColorSchemeResources(android.R.color.holo_red_dark)
        binding.swipeRefresh.setOnRefreshListener { loadAllData() }

        binding.layoutAvatar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfilFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.ivNotifikasi.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, NotifikasiFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnViewGeser.setOnClickListener {
            if (!isViewGeser) {
                isViewGeser = true
                updateToggleUI()
                applyLayoutManager()
                if (currentPesananList.isNotEmpty()) tampilkanDaftar(currentPesananList, currentIsActive)
            }
        }

        binding.btnViewBawah.setOnClickListener {
            if (isViewGeser) {
                isViewGeser = false
                updateToggleUI()
                applyLayoutManager()
                if (currentPesananList.isNotEmpty()) tampilkanDaftar(currentPesananList, currentIsActive)
            }
        }

        updateToggleUI()
        loadAllData()
    }

    override fun onResume() {
        super.onResume()
        loadFotoProfil()
    }

    private fun loadFotoProfil() {
        if (_binding == null) return

        val cachedUrl = session.getFotoProfil()
        if (cachedUrl.isNotEmpty()) {
            Glide.with(this)
                .load(cachedUrl)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
        }

        val token = session.getToken()
        val baseUrl = session.getBaseUrl()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("$baseUrl/api/driver/profile")
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@launch

                if (response.isSuccessful) {
                    val json = org.json.JSONObject(body)
                    val data = json.getJSONObject("data")
                    val fotoUrl = if (data.isNull("foto_url")) null else data.getString("foto_url")

                    if (!fotoUrl.isNullOrEmpty()) {
                        session.saveFotoProfil(fotoUrl)
                        withContext(Dispatchers.Main) {
                            if (_binding == null) return@withContext
                            Glide.with(this@BerandaFragment)
                                .load(fotoUrl)
                                .circleCrop()
                                .into(binding.ivAvatar)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyLayoutManager() {
        if (isViewGeser) {
            binding.rvPesananMasuk.layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        } else {
            binding.rvPesananMasuk.layoutManager = LinearLayoutManager(requireContext())
        }
        binding.rvPesananMasuk.isNestedScrollingEnabled = false
    }

    private fun updateToggleUI() {
        if (isViewGeser) {
            binding.btnViewGeser.setBackgroundResource(R.drawable.bg_toggle_selected)
            binding.btnViewGeser.setColorFilter(requireContext().getColor(android.R.color.white))
            binding.btnViewBawah.setBackgroundResource(android.R.color.transparent)
            binding.btnViewBawah.setColorFilter(requireContext().getColor(android.R.color.darker_gray))
        } else {
            binding.btnViewBawah.setBackgroundResource(R.drawable.bg_toggle_selected)
            binding.btnViewBawah.setColorFilter(requireContext().getColor(android.R.color.white))
            binding.btnViewGeser.setBackgroundResource(android.R.color.transparent)
            binding.btnViewGeser.setColorFilter(requireContext().getColor(android.R.color.darker_gray))
        }
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

                        val pesananAktif = list.find {
                            it.kurirId == myId && it.statusAntar?.lowercase() == "sedang diantar"
                        }

                        if (pesananAktif != null) {
                            isViewGeser = false
                            updateToggleUI()
                            tampilkanDaftar(listOf(pesananAktif), isActive = true)
                        } else {
                            val rejectedIds = session.getRejectedPesananIds()
                            val antrian = list.filter {
                                it.statusAntar?.lowercase() == "diproses" && it.kurirId == null
                                        && it.id.toString() !in rejectedIds
                            }
                            if (antrian.isNotEmpty()) {
                                tampilkanDaftar(antrian, isActive = false)
                            } else {
                                tampilKosong()
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.swipeRefresh.isRefreshing = false
                }
            })
    }

    private fun tampilkanDaftar(list: List<Pesanan>, isActive: Boolean) {
        currentPesananList = list
        currentIsActive = isActive

        binding.rvPesananMasuk.visibility = View.VISIBLE
        binding.layoutPesananKosong.visibility = View.GONE

        applyLayoutManager()

        binding.rvPesananMasuk.adapter = PesananMasukAdapter(
            list = list,
            viewMode = if (isViewGeser) PesananMasukAdapter.VIEW_GESER else PesananMasukAdapter.VIEW_BAWAH,
            isActive = isActive,
            onAccept = { pesanan ->
                if (isActive) selesaikanPesanan(pesanan.id) else claimPesanan(pesanan.id)
            },
            onReject = { pesanan ->
                if (isActive) batalkanPesanan(pesanan.id) else {
                    session.rejectPesananLokal(pesanan.id)
                    Toast.makeText(requireContext(), "Pesanan ditolak.", Toast.LENGTH_SHORT).show()
                    loadPesanan()
                }
            },
            onItemClick = { pesanan -> bukaDetailPesanan(pesanan) }
        )
    }

    private fun tampilKosong() {
        currentPesananList = emptyList()
        binding.rvPesananMasuk.visibility = View.GONE
        binding.layoutPesananKosong.visibility = View.VISIBLE
    }

    private fun bukaDetailPesanan(pesanan: Pesanan) {
        val detailFragment = DetailPesananFragment.newInstance(pesanan.id)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, detailFragment)
            .addToBackStack(null)
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

    private fun batalkanPesanan(id: Int) {
        ApiClient.instance.batalkanPesanan(session.getBearerToken(), id)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Pesanan dibatalkan.", Toast.LENGTH_SHORT).show()
                        loadAllData()
                    } else {
                        Toast.makeText(requireContext(), "Gagal batalkan: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), "Error batalkan: ${t.message}", Toast.LENGTH_LONG).show()
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
                        val listRaw = response.body()?.data ?: emptyList()
                        binding.rvRiwayat.adapter = RiwayatAdapter(listRaw)
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