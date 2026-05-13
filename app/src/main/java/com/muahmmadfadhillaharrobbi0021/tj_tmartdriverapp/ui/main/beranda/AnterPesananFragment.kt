package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentAnterPesananBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.Pesanan
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.UpdateStatusRequest
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.UpdateStatusResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class AnterPesananFragment : Fragment() {

    private var _binding: FragmentAnterPesananBinding? = null
    private val binding get() = _binding!!

    private var pesananId: Int = 0
    private lateinit var session: SessionManager

    // Live location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var driverLat: Double = 0.0
    private var driverLng: Double = 0.0
    private var destLat: Double = 0.0
    private var destLng: Double = 0.0

    // Timer — hanya UI runnable, state ada di companion
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    // State
    private var pesanan: Pesanan? = null
    private var isSelesai = false

    // Auto-chat: hanya kirim sekali saat ≤ 5 meter
    private var autoChatSent = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startLocationUpdates()
        else Toast.makeText(requireContext(), "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(id: Int) = AnterPesananFragment().apply {
            arguments = Bundle().apply { putInt("PESANAN_ID", id) }
        }

        // Timer global — tidak reset saat fragment recreate
        private var globalStartTime = 0L
        private var globalTimerRunning = false
        private var globalElapsedSeconds = 0L

        fun resetTimer() {
            globalStartTime = 0L
            globalTimerRunning = false
            globalElapsedSeconds = 0L
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pesananId = arguments?.getInt("PESANAN_ID") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnterPesananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupTimeline()
        fetchDetailPesanan()
        startTimer()

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnHubungiPembeli.setOnClickListener { hubungiViaWhatsApp() }
        binding.btnChat.setOnClickListener { bukaChat() }
        binding.btnPesananTiba.setOnClickListener { konfirmasiTiba() }
    }

    private fun setupTimeline() {
        with(binding) {
            layoutStep1.alpha = 0.4f
            layoutStep2.alpha = 0.4f
            layoutStep3.alpha = 1f
            layoutStep4.alpha = 0.4f
        }
    }

    private fun markTiba() {
        with(binding) {
            layoutStep4.alpha = 1f
            ivStep4.setImageResource(R.drawable.ic_check_circle)
        }
    }

    private fun fetchDetailPesanan() {
        binding.progressBar.visibility = View.VISIBLE
        ApiClient.instance.getPesanan(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(call: Call<PesananResponse>, response: Response<PesananResponse>) {
                    if (_binding == null) return
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val detail = response.body()?.data?.find { it.id == pesananId }
                        detail?.let {
                            pesanan = it
                            setupUI(it)
                            startLocationUpdates()
                        }
                    }
                }
                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal memuat: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupUI(p: Pesanan) {
        val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
        with(binding) {
            tvNamaPembeli.text      = p.user?.name ?: "Pelanggan"
            tvTeleponPembeli.text   = p.user?.noTelp ?: "-"
            tvAlamatTujuan.text     = p.alamatDisplay ?: "Alamat tidak tersedia"
            tvMetodePembayaran.text = p.pembayaranDisplay ?: "Cash"
            tvTotalHarga.text       = nf.format(p.totalHarga ?: 0)
        }

        val namaGedung = p.alamatDisplay?.split(",")?.firstOrNull()?.trim()
            ?: p.user?.alamatGedung ?: "Gedung 5"
        val raw = getCoords(namaGedung).split(",")
        destLat = raw[0].toDoubleOrNull() ?: -6.9706729
        destLng = raw[1].toDoubleOrNull() ?: 107.627767
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(2f).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                driverLat = loc.latitude
                driverLng = loc.longitude
                updateMap()
                updateJarak()
            }
        }

        fusedLocationClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                driverLat = loc.latitude
                driverLng = loc.longitude
                updateMap()
                updateJarak()
            }
        }
    }

    private fun updateMap() {
        if (_binding == null) return
        val html = buildMapHtml(driverLat, driverLng, destLat, destLng)
        with(binding.webViewTracking.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        binding.webViewTracking.loadDataWithBaseURL(
            "https://www.google.com", html, "text/html", "UTF-8", null
        )
    }

    private fun buildMapHtml(dLat: Double, dLng: Double, tLat: Double, tLng: Double): String {
        val apiKey = "AIzaSyBUJmPMTiRWnPLl4b3UtQC2CC8abm41yzA"
        return """
        <!DOCTYPE html><html>
        <head><meta name="viewport" content="width=device-width,initial-scale=1">
        <style>html,body,#map{height:100%;margin:0;padding:0}
        .legend{position:absolute;bottom:20px;left:50%;transform:translateX(-50%);background:white;
        border-radius:20px;padding:8px 16px;display:flex;gap:12px;font-size:12px;
        box-shadow:0 2px 8px rgba(0,0,0,.2);font-family:sans-serif;z-index:999}
        .dot{width:10px;height:10px;border-radius:50%;display:inline-block;margin-right:4px}</style>
        </head><body>
        <div id="map"></div>
        <div class="legend">
          <span><span class="dot" style="background:#1a73e8"></span>Driver</span>
          <span><span class="dot" style="background:#ea4335"></span>Tujuan</span>
        </div>
        <script>
        function initMap(){
          const driver={lat:$dLat,lng:$dLng}, dest={lat:$tLat,lng:$tLng};
          const map=new google.maps.Map(document.getElementById('map'),{
            zoom:16,center:driver,mapTypeControl:false,streetViewControl:false,fullscreenControl:false});
          new google.maps.Marker({position:driver,map,title:'Driver',
            icon:{path:google.maps.SymbolPath.CIRCLE,scale:8,fillColor:'#1a73e8',
            fillOpacity:1,strokeColor:'white',strokeWeight:2}});
          new google.maps.Marker({position:dest,map,title:'Tujuan'});
          const b=new google.maps.LatLngBounds();b.extend(driver);b.extend(dest);map.fitBounds(b);
        }
        </script>
        <script src="https://maps.googleapis.com/maps/api/js?key=$apiKey&callback=initMap&loading=async" async defer></script>
        </body></html>
        """.trimIndent()
    }

    private fun updateJarak() {
        if (destLat == 0.0 || driverLat == 0.0) return
        val results = FloatArray(1)
        Location.distanceBetween(driverLat, driverLng, destLat, destLng, results)
        val meter = results[0].toInt()

        val teks = if (meter >= 1000) String.format("Jarak: %.1f km", meter / 1000f)
        else "Jarak: $meter m"
        binding.tvJarak.text = teks

        if (meter <= 5 && !autoChatSent) {
            autoChatSent = true
            kirimAutoChatHampirTiba()
            binding.bannerDekat.visibility = View.VISIBLE
        }
    }

    private fun kirimAutoChatHampirTiba() {
        val fragment = ChatPesananFragment.newInstance(
            pesananId    = pesananId,
            namaPembeli  = pesanan?.user?.name ?: "Pelanggan",
            nomorPembeli = pesanan?.user?.noTelp ?: "",
            autoPesan    = "Pesanan Anda sebentar lagi telah sampai! 🛵",
            fotoPembeli  = pesanan?.user?.gambar
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ─── Timer ────────────────────────────────────────────────────────────────

    private fun startTimer() {
        // Mulai timer baru hanya jika belum berjalan
        if (!globalTimerRunning) {
            globalStartTime = System.currentTimeMillis()
            globalTimerRunning = true
        }

        timerRunnable = object : Runnable {
            override fun run() {
                if (!globalTimerRunning) return
                // Hitung dari startTime agar tetap akurat walau fragment recreate
                globalElapsedSeconds = (System.currentTimeMillis() - globalStartTime) / 1000
                val jam = globalElapsedSeconds / 3600
                val mnt = (globalElapsedSeconds % 3600) / 60
                val dtk = globalElapsedSeconds % 60
                if (_binding == null) return
                binding.tvTimer.text = if (jam > 0)
                    String.format("%02d:%02d:%02d", jam, mnt, dtk)
                else
                    String.format("%02d:%02d", mnt, dtk)
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.post(timerRunnable!!)
    }

    private fun hubungiViaWhatsApp() {
        val rawPhone = pesanan?.user?.noTelp

        if (rawPhone.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val normalized = normalizePhone(rawPhone)
        val pm = requireActivity().packageManager
        val waPackage = "com.whatsapp"
        val waBusinessPackage = "com.whatsapp.w4b"

        val isWaInstalled = try { pm.getPackageInfo(waPackage, 0); true }
        catch (e: PackageManager.NameNotFoundException) { false }

        val isWaBusinessInstalled = try { pm.getPackageInfo(waBusinessPackage, 0); true }
        catch (e: PackageManager.NameNotFoundException) { false }

        when {
            isWaInstalled || isWaBusinessInstalled -> {
                try {
                    val uri = Uri.parse("https://wa.me/$normalized")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        if (isWaInstalled) setPackage(waPackage)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$normalized")))
                }
            }
            else -> {
                Toast.makeText(requireContext(), "WhatsApp tidak ditemukan. Menghubungi via telepon...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$rawPhone")))
            }
        }
    }

    private fun normalizePhone(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        return when {
            cleaned.startsWith("+62") -> cleaned.removePrefix("+")
            cleaned.startsWith("62")  -> cleaned
            cleaned.startsWith("0")   -> "62" + cleaned.removePrefix("0")
            else                       -> "62$cleaned"
        }
    }

    private fun bukaChat() {
        val p = pesanan
        if (p == null) {
            Toast.makeText(requireContext(), "Data pesanan belum siap", Toast.LENGTH_SHORT).show()
            return
        }

        val fragment = ChatPesananFragment.newInstance(
            pesananId    = pesananId,
            namaPembeli  = p.user?.name ?: "Pelanggan",
            nomorPembeli = p.user?.noTelp ?: "",
            autoPesan    = null,
            fotoPembeli  = p.user?.gambar
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun konfirmasiTiba() {
        if (isSelesai) return
        isSelesai = true
        globalTimerRunning = false  // ← stop timer global
        binding.btnPesananTiba.isEnabled = false
        binding.btnPesananTiba.text = "Memproses..."
        timerRunnable?.let { timerHandler.removeCallbacks(it) }

        val jarakTeks = binding.tvJarak.text.toString()
            .replace("Jarak: ", "").trim()
        val jam = globalElapsedSeconds / 3600
        val mnt = (globalElapsedSeconds % 3600) / 60
        val dtk = globalElapsedSeconds % 60
        val durasiTeks = if (jam > 0)
            String.format("%d,%02d jam", jam, mnt)
        else
            String.format("%d,%02d menit", mnt, dtk)

        ApiClient.instance.updateStatusAntar(
            session.getBearerToken(),
            pesananId,
            UpdateStatusRequest(
                status_antar = "tiba",
                jarak = jarakTeks,
                durasi = durasiTeks
            )
        ).enqueue(object : Callback<UpdateStatusResponse> {
            override fun onResponse(
                call: Call<UpdateStatusResponse>, response: Response<UpdateStatusResponse>
            ) {
                if (_binding == null) return
                if (response.isSuccessful) {
                    markTiba()
                    session.tambahPesananHariIni()
                    resetTimer()  // ← reset setelah pesanan selesai
                    val tibaFragment = PesananTibaFragment.newInstance(
                        namaPembeli = pesanan?.user?.name ?: "Pelanggan",
                        totalHarga  = pesanan?.totalHarga ?: 0
                    )
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, tibaFragment)
                        .commit()
                } else {
                    isSelesai = false
                    globalTimerRunning = true
                    binding.btnPesananTiba.isEnabled = true
                    binding.btnPesananTiba.text = "✓ Pesanan Tiba"
                    Toast.makeText(requireContext(), "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateStatusResponse>, t: Throwable) {
                if (_binding == null) return
                isSelesai = false
                globalTimerRunning = true
                binding.btnPesananTiba.isEnabled = true
                binding.btnPesananTiba.text = "✓ Pesanan Tiba"
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        // globalTimerRunning JANGAN diubah di sini agar timer tetap jalan di background
        if (::locationCallback.isInitialized)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }
}