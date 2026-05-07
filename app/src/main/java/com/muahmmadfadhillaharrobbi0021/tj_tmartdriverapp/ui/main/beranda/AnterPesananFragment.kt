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

    // Timer
    private var timerHandler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0L
    private var timerRunnable: Runnable? = null

    // Data pesanan
    private var pesanan: Pesanan? = null
    private var isSelesai = false
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startLocationUpdates()
        } else {
            Toast.makeText(requireContext(), "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(id: Int) = AnterPesananFragment().apply {
            arguments = Bundle().apply { putInt("PESANAN_ID", id) }
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

        binding.btnNavigasi.setOnClickListener { openGoogleMapsNavigation() }

        binding.btnHubungiPembeli.setOnClickListener { dialPembeli() }

        binding.btnPesananTiba.setOnClickListener { konfirmasiTiba() }
    }


    private fun setupTimeline() {
        // Set status awal: "Diantar" aktif
        with(binding) {
            tvStep1.alpha = 0.4f
            tvStep2.alpha = 0.4f
            tvStep3.alpha = 1f   // Diantar = aktif
            tvStep4.alpha = 0.4f
            ivStep1.alpha = 0.4f
            ivStep2.alpha = 0.4f
            ivStep3.alpha = 1f
            ivStep4.alpha = 0.4f
        }
    }

    private fun markTiba() {
        with(binding) {
            tvStep4.alpha = 1f
            ivStep4.alpha = 1f
            ivStep4.setImageResource(R.drawable.ic_check_circle)
        }
    }


    private fun fetchDetailPesanan() {
        binding.progressBar.visibility = View.VISIBLE
        ApiClient.instance.getPesanan(session.getBearerToken())
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(
                    call: Call<PesananResponse>, response: Response<PesananResponse>
                ) {
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
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal memuat: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupUI(p: Pesanan) {
        val localeID = Locale("in", "ID")
        val nf = NumberFormat.getCurrencyInstance(localeID).apply {
            maximumFractionDigits = 0
        }

        with(binding) {
            tvNamaPembeli.text    = p.user?.name ?: "Pelanggan"
            // Pastikan p.user tidak null saat akses
            tvTeleponPembeli.text = p.user?.noTelp ?: "-"
            tvAlamatTujuan.text   = p.alamatDisplay ?: "Alamat tidak tersedia"
            tvMetodePembayaran.text = p.pembayaranDisplay ?: "Cash"
            tvTotalHarga.text       = nf.format(p.totalHarga ?: 0)
            tvIdPesanan.text        = "#${p.id}"
        }

        // Ambil coords tujuan
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
            .setMinUpdateDistanceMeters(10f).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location = result.lastLocation ?: return
                driverLat = loc.latitude
                driverLng = loc.longitude
                updateMap()
                updateJarak()
            }
        }

        fusedLocationClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())

        // Ambil lokasi terakhir segera
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
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
            /* Penting: Pastikan height 100% agar tidak putih */
            html, body, #map { height: 100%; margin: 0; padding: 0; }
            .legend {
                position:absolute; bottom:20px; left:50%; transform:translateX(-50%);
                background:white; border-radius:20px; padding:8px 16px;
                display:flex; gap:12px; font-size:12px; box-shadow:0 2px 8px rgba(0,0,0,0.2);
                font-family:sans-serif; z-index: 999;
            }
            .dot { width:10px; height:10px; border-radius:50%; display:inline-block; margin-right:4px; }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <div class="legend">
            <span><span class="dot" style="background:#1a73e8"></span> Driver</span>
            <span><span class="dot" style="background:#ea4335"></span> Tujuan</span>
        </div>
        <script>
            function initMap() {
                const driver = {lat: $dLat, lng: $dLng};
                const dest   = {lat: $tLat, lng: $tLng};
                
                const map = new google.maps.Map(document.getElementById('map'), {
                    zoom: 16,
                    center: driver,
                    mapTypeControl: false,
                    streetViewControl: false,
                    fullscreenControl: false
                });

                new google.maps.Marker({
                    position: driver, map, title: 'Driver',
                    icon: { path: google.maps.SymbolPath.CIRCLE, scale: 8, fillColor: '#1a73e8', fillOpacity: 1, strokeColor: 'white', strokeWeight: 2 }
                });

                new google.maps.Marker({
                    position: dest, map, title: 'Tujuan'
                });

                const bounds = new google.maps.LatLngBounds();
                bounds.extend(driver);
                bounds.extend(dest);
                map.fitBounds(bounds);
            }
        </script>
        <!-- Tambahkan callback=initMap dan loading=async -->
        <script src="https://maps.googleapis.com/maps/api/js?key=$apiKey&callback=initMap&loading=async" async defer></script>
    </body>
    </html>
    """.trimIndent()
    }

    private fun updateJarak() {
        if (destLat == 0.0 || driverLat == 0.0) return
        val results = FloatArray(1)
        Location.distanceBetween(driverLat, driverLng, destLat, destLng, results)
        val meter = results[0].toInt()
        val teks = if (meter >= 1000) String.format("%.1f km", meter / 1000f)
        else "$meter m"
        binding.tvJarak.text = "Jarak: $teks"
    }

    // Timer

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                elapsedSeconds++
                val jam  = elapsedSeconds / 3600
                val mnt  = (elapsedSeconds % 3600) / 60
                val dtk  = elapsedSeconds % 60
                val teks = if (jam > 0) String.format("%02d:%02d:%02d", jam, mnt, dtk)
                else String.format("%02d:%02d", mnt, dtk)
                binding.tvTimer.text = teks
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.post(timerRunnable!!)
    }

    // Aksi Tombol

    private fun openGoogleMapsNavigation() {
        if (destLat == 0.0) return
        val uri = Uri.parse("google.navigation:q=$destLat,$destLng")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
        if (intent.resolveActivity(requireActivity().packageManager) != null) startActivity(intent)
        else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")))
    }

    private fun dialPembeli() {
        val user = pesanan?.user
        val phoneNum = user?.noTelp

        if (phoneNum.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Nomor tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
        startActivity(intent)
    }

    private fun konfirmasiTiba() {
        if (isSelesai) return
        isSelesai = true
        binding.btnPesananTiba.isEnabled = false
        binding.btnPesananTiba.text = "Memproses..."

        // Hentikan timer
        timerRunnable?.let { timerHandler.removeCallbacks(it) }

        // Update status ke backend (kirim email otomatis dari server)
        ApiClient.instance.updateStatusAntar(
            session.getBearerToken(),
            pesananId,
            UpdateStatusRequest(status_antar = "tiba")
        ).enqueue(object : Callback<UpdateStatusResponse> {
            override fun onResponse(
                call: Call<UpdateStatusResponse>, response: Response<UpdateStatusResponse>
            ) {
                if (_binding == null) return
                if (response.isSuccessful) {
                    markTiba()
                    binding.btnPesananTiba.text = "✓ Pesanan Tiba!"
                    Toast.makeText(requireContext(), "Pesanan selesai! Email notifikasi terkirim ke pembeli.", Toast.LENGTH_LONG).show()
                    // Kembali ke beranda setelah 2 detik
                    timerHandler.postDelayed({ parentFragmentManager.popBackStack() }, 2000)
                } else {
                    isSelesai = false
                    binding.btnPesananTiba.isEnabled = true
                    binding.btnPesananTiba.text = "Pesanan Tiba"
                    Toast.makeText(requireContext(), "Gagal update status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateStatusResponse>, t: Throwable) {
                isSelesai = false
                binding.btnPesananTiba.isEnabled = true
                binding.btnPesananTiba.text = "Pesanan Tiba"
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ─── Koordinat Gedung ─────────────────────────────────────────────────────

    private fun getCoords(gedungName: String): String = when (gedungName) {
        "Gedung 1"  -> "-6.9710403,107.6283141"
        "Gedung 2"  -> "-6.9707509,107.6283404"
        "Gedung 3"  -> "-6.9704344,107.6283533"
        "Gedung 4"  -> "-6.9709904,107.6277174"
        "Gedung 5"  -> "-6.9706729,107.627767"
        "Gedung 6"  -> "-6.970935,107.6271111"
        "Gedung 7"  -> "-6.9706223,107.6271815"
        "Gedung 8"  -> "-6.9702831,107.6272323"
        "Gedung 9"  -> "-6.9700347,107.6277742"
        "Gedung 10" -> "-6.9697409,107.6278167"
        "Gedung 11" -> "-6.9700978,107.6283584"
        "Gedung 12" -> "-6.9697555,107.6283976"
        "Gedung A"  -> "-6.9740468,107.6285963"
        "Gedung B"  -> "-6.9736757,107.6286558"
        "Gedung C"  -> "-6.9732535,107.6287044"
        "Gedung D"  -> "-6.9728527,107.6286204"
        "Gedung E"  -> "-6.9725544,107.6286242"
        "Gedung F"  -> "-6.9720839,107.6286579"
        else        -> "-6.9706729,107.627767"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        if (::locationCallback.isInitialized) fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }
}