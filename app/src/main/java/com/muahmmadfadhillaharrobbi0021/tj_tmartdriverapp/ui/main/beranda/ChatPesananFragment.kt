package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentChatPesananBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TipePesan { TEKS, GAMBAR, VIDEO }

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val pengirim: String,           // "driver" | "customer"
    val isi: String,                // teks atau path URI media
    val tipe: TipePesan = TipePesan.TEKS,
    val waktu: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)
class ChatPesananFragment : Fragment() {

    private var _binding: FragmentChatPesananBinding? = null
    private val binding get() = _binding!!

    private var pesananId: Int = 0
    private var namaPembeli: String = "Pelanggan"
    private var nomorPembeli: String = ""
    private var autoPesan: String? = null
    private var fotoPembeli: String? = null

    private var latTujuan: Double = 0.0
    private var lngTujuan: Double = 0.0

    private var sudahKirimPesanTiba = false

    private val pesanList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    private var photoUri: Uri? = null
    private var selectedMediaUri: Uri? = null
    private var selectedMediaType: TipePesan = TipePesan.GAMBAR

    // FusedLocation untuk deteksi 5 meter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    // ── Launchers ─────────────────────────────────────────────────────────────

    private val launcherKameraFoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok && photoUri != null) tampilkanPreviewMedia(photoUri!!, TipePesan.GAMBAR, "1 foto siap dikirim") }

    private val launcherGaleriFoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { tampilkanPreviewMedia(it, TipePesan.GAMBAR, "1 foto dipilih") } }

    private val launcherGaleriVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { tampilkanPreviewMedia(it, TipePesan.VIDEO, "1 video dipilih") } }

    private val launcherIzinKamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) bukaKamera() else Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show() }

    private val launcherIzinTelepon = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.RECORD_AUDIO] == true) mulaiPanggilan()
        else Toast.makeText(requireContext(), "Izin mikrofon diperlukan", Toast.LENGTH_SHORT).show()
    }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val ARG_PESANAN_ID  = "pesanan_id"
        private const val ARG_NAMA        = "nama_pembeli"
        private const val ARG_NOMOR       = "nomor_pembeli"
        private const val ARG_AUTO_PESAN  = "auto_pesan"
        private const val ARG_FOTO        = "foto_pembeli"
        private const val ARG_LAT_TUJUAN  = "lat_tujuan"
        private const val ARG_LNG_TUJUAN  = "lng_tujuan"

        fun newInstance(
            pesananId: Int,
            namaPembeli: String,
            nomorPembeli: String,
            autoPesan: String?,
            fotoPembeli: String? = null,
            latTujuan: Double = 0.0,
            lngTujuan: Double = 0.0
        ) = ChatPesananFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PESANAN_ID, pesananId)
                putString(ARG_NAMA, namaPembeli)
                putString(ARG_NOMOR, nomorPembeli)
                putString(ARG_AUTO_PESAN, autoPesan)
                putString(ARG_FOTO, fotoPembeli)
                putDouble(ARG_LAT_TUJUAN, latTujuan)
                putDouble(ARG_LNG_TUJUAN, lngTujuan)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pesananId   = it.getInt(ARG_PESANAN_ID)
            namaPembeli = it.getString(ARG_NAMA, "Pelanggan")
            nomorPembeli= it.getString(ARG_NOMOR, "")
            autoPesan   = it.getString(ARG_AUTO_PESAN)
            fotoPembeli = it.getString(ARG_FOTO)
            latTujuan   = it.getDouble(ARG_LAT_TUJUAN)
            lngTujuan   = it.getDouble(ARG_LNG_TUJUAN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatPesananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        setupRecyclerView()
        setupInputListeners()
        setupQuickReply()
        mulaiPantauLokasi()

        autoPesan?.let { pesan ->
            tambahPesan(ChatMessage(pengirim = "driver", isi = pesan))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        _binding = null
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private fun setupHeader() {
        binding.tvNamaChatHeader.text = namaPembeli

        if (!fotoPembeli.isNullOrBlank()) {
            com.bumptech.glide.Glide.with(this)
                .load("${com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.Constants.BASE_URL}storage/$fotoPembeli")
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivAvatarChat)
        }

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnVoipCall.setOnClickListener {
            val perlu = arrayOf(Manifest.permission.RECORD_AUDIO)
            val ok = perlu.all {
                ActivityCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
            }
            if (ok) mulaiPanggilan() else launcherIzinTelepon.launch(perlu)
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(pesanList)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun setupInputListeners() {
        // TextWatcher dihapus — kamera selalu tampil (statis)

        binding.btnKamera.setOnClickListener { tampilkanPopupKamera() }

        binding.btnKirimPesan.setOnClickListener { kirimTeks() }

        binding.btnBatalMedia.setOnClickListener { resetMediaPreview() }
    }

    private fun setupQuickReply() {
        // Quick reply chips — tampil setelah pesan otomatis tiba dikirim
        binding.chipPesananSesuai.setOnClickListener {
            kirimQuickReply("✅ Pesanan sudah sesuai")
        }
        binding.chipTitikSesuai.setOnClickListener {
            kirimQuickReply("📍 Titik pengantaran sesuai")
        }
        binding.chipBeliLagi.setOnClickListener {
            kirimQuickReply("🛒 Ada barang lain yang mau dibeli?")
        }
    }

    private fun kirimQuickReply(teks: String) {
        tambahPesan(ChatMessage(pengirim = "driver", isi = teks))
        // TODO: kirim via API/Firestore
    }

    // ── Lokasi — auto pesan 5 meter ───────────────────────────────────────────

    private fun mulaiPantauLokasi() {
        if (latTujuan == 0.0 && lngTujuan == 0.0) return  // koordinat tujuan belum diset

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(1f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val lokasi = result.lastLocation ?: return
                cekJarakKeTujuan(lokasi)
            }
        }

        val izinOk = ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (izinOk) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
        }
    }

    private fun cekJarakKeTujuan(lokasi: Location) {
        if (sudahKirimPesanTiba) return

        val tujuan = Location("tujuan").apply {
            latitude  = latTujuan
            longitude = lngTujuan
        }

        val jarakMeter = lokasi.distanceTo(tujuan)

        if (jarakMeter <= 5f) {
            sudahKirimPesanTiba = true
            val pesanTiba = "🛵 Pesanan Anda akan segera tiba! Driver sedang dalam jarak dekat."
            tambahPesan(ChatMessage(pengirim = "driver", isi = pesanTiba))
            // Tampilkan quick reply setelah pesan tiba
            binding.scrollQuickReply.visibility = View.VISIBLE
            // TODO: kirim via API/Firestore
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        }
    }

    private fun tampilkanPopupKamera() {
        val opsi = arrayOf("📷 Foto", "🎥 Video")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Kirim Media")
            .setItems(opsi) { _, which ->
                when (which) {
                    0 -> pilihSumberFoto()
                    1 -> launcherGaleriVideo.launch("video/*")
                }
            }.show()
    }

    private fun kirimTeks() {
        val teks = binding.etPesan.text?.toString()?.trim()

        if (selectedMediaUri != null) {
            tambahPesan(ChatMessage(pengirim = "driver", isi = selectedMediaUri.toString(), tipe = selectedMediaType))
            if (!teks.isNullOrEmpty()) tambahPesan(ChatMessage(pengirim = "driver", isi = teks))
            resetMediaPreview()
            binding.etPesan.setText("")
            return
        }

        if (teks.isNullOrEmpty()) return
        tambahPesan(ChatMessage(pengirim = "driver", isi = teks))
        binding.etPesan.setText("")
        // TODO: kirim via API/Firestore
    }

    private fun tambahPesan(pesan: ChatMessage) {
        pesanList.add(pesan)
        chatAdapter.notifyItemInserted(pesanList.lastIndex)
        binding.rvChat.post { binding.rvChat.scrollToPosition(pesanList.lastIndex) }
    }

    private fun pilihSumberFoto() {
        val opsi = arrayOf("📷 Ambil Foto", "🖼️ Pilih dari Galeri")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Kirim Foto")
            .setItems(opsi) { _, which ->
                when (which) {
                    0 -> {
                        val izin = Manifest.permission.CAMERA
                        if (ActivityCompat.checkSelfPermission(requireContext(), izin) == PackageManager.PERMISSION_GRANTED)
                            bukaKamera()
                        else launcherIzinKamera.launch(izin)
                    }
                    1 -> launcherGaleriFoto.launch("image/*")
                }
            }.show()
    }

    private fun bukaKamera() {
        val file = File.createTempFile("chat_foto_${System.currentTimeMillis()}", ".jpg", requireContext().cacheDir)
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        launcherKameraFoto.launch(photoUri)
    }

    private fun tampilkanPreviewMedia(uri: Uri, tipe: TipePesan, label: String) {
        selectedMediaUri  = uri
        selectedMediaType = tipe
        with(binding) {
            layoutMediaPreview.visibility = View.VISIBLE
            tvMediaLabel.text = label
            if (tipe == TipePesan.GAMBAR) ivMediaPreview.setImageURI(uri)
            else ivMediaPreview.setImageResource(R.drawable.ic_videocam)
        }
    }

    private fun resetMediaPreview() {
        selectedMediaUri = null
        binding.layoutMediaPreview.visibility = View.GONE
        binding.ivMediaPreview.setImageDrawable(null)
    }

    private fun mulaiPanggilan() {
        if (nomorPembeli.isBlank()) {
            Toast.makeText(requireContext(), "Nomor tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$nomorPembeli")))
    }
}