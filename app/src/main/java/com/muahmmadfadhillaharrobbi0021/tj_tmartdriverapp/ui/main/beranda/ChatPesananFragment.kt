package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentChatPesananBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Model Pesan ──────────────────────────────────────────────────────────────

enum class TipePesan { TEKS, GAMBAR, VIDEO }

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val pengirim: String,           // "driver" | "customer"
    val isi: String,                // teks atau path URI media
    val tipe: TipePesan = TipePesan.TEKS,
    val waktu: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
)

// ─── Fragment ─────────────────────────────────────────────────────────────────

class ChatPesananFragment : Fragment() {

    private var _binding: FragmentChatPesananBinding? = null
    private val binding get() = _binding!!

    private var pesananId: Int = 0
    private var namaPembeli: String = "Pelanggan"
    private var nomorPembeli: String = ""
    private var autoPesan: String? = null

    private val pesanList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    // URI foto yang diambil kamera
    private var photoUri: Uri? = null
    private var selectedMediaUri: Uri? = null
    private var selectedMediaType: TipePesan = TipePesan.GAMBAR

    // ── Activity Result Launchers ──────────────────────────────────────────────

    /** Launcher kamera foto */
    private val launcherKameraFoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { berhasil ->
        if (berhasil && photoUri != null) {
            tampilkanPreviewMedia(photoUri!!, TipePesan.GAMBAR, "1 foto siap dikirim")
        }
    }

    /** Launcher galeri foto */
    private val launcherGaleriFoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { tampilkanPreviewMedia(it, TipePesan.GAMBAR, "1 foto dipilih") }
    }

    /** Launcher galeri video */
    private val launcherGaleriVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { tampilkanPreviewMedia(it, TipePesan.VIDEO, "1 video dipilih") }
    }

    /** Launcher izin kamera */
    private val launcherIzinKamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) bukaKamera() else
            Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
    }

    /** Launcher izin telepon untuk VoIP */
    private val launcherIzinTelepon = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val recordOk = perms[Manifest.permission.RECORD_AUDIO] == true
        if (recordOk) mulaiPanggilan()
        else Toast.makeText(requireContext(), "Izin mikrofon diperlukan untuk panggilan", Toast.LENGTH_SHORT).show()
    }

    // ─── Companion ────────────────────────────────────────────────────────────

    companion object {
        private const val ARG_PESANAN_ID   = "pesanan_id"
        private const val ARG_NAMA         = "nama_pembeli"
        private const val ARG_NOMOR        = "nomor_pembeli"
        private const val ARG_AUTO_PESAN   = "auto_pesan"

        fun newInstance(
            pesananId: Int,
            namaPembeli: String,
            nomorPembeli: String,
            autoPesan: String?
        ) = ChatPesananFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PESANAN_ID, pesananId)
                putString(ARG_NAMA, namaPembeli)
                putString(ARG_NOMOR, nomorPembeli)
                putString(ARG_AUTO_PESAN, autoPesan)
            }
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pesananId    = it.getInt(ARG_PESANAN_ID)
            namaPembeli  = it.getString(ARG_NAMA, "Pelanggan")
            nomorPembeli = it.getString(ARG_NOMOR, "")
            autoPesan    = it.getString(ARG_AUTO_PESAN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatPesananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupRecyclerView()
        setupInputListeners()

        // Kirim auto-pesan jika ada (misal: "Sebentar lagi sampai!")
        autoPesan?.let { pesan ->
            tambahPesan(ChatMessage(pengirim = "driver", isi = pesan, tipe = TipePesan.TEKS))
            // TODO: kirim juga ke backend / Firestore agar customer menerima
        }
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private fun setupHeader() {
        binding.tvNamaChatHeader.text = namaPembeli

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Tombol VoIP — cek izin lalu mulai panggilan
        binding.btnVoipCall.setOnClickListener {
            val perluIzin = arrayOf(Manifest.permission.RECORD_AUDIO)
            val semuaGranted = perluIzin.all {
                ActivityCompat.checkSelfPermission(requireContext(), it) ==
                        PackageManager.PERMISSION_GRANTED
            }
            if (semuaGranted) mulaiPanggilan() else launcherIzinTelepon.launch(perluIzin)
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(pesanList)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).also { it.stackFromEnd = true }
            adapter = chatAdapter
        }
    }

    private fun setupInputListeners() {
        // Kirim teks
        binding.btnKirimPesan.setOnClickListener { kirimTeks() }

        // Foto: pilih sumber (kamera/galeri)
        binding.btnKirimFoto.setOnClickListener { pilihSumberFoto() }

        // Video: galeri
        binding.btnKirimVideo.setOnClickListener { launcherGaleriVideo.launch("video/*") }

        // Batal media preview
        binding.btnBatalMedia.setOnClickListener { resetMediaPreview() }
    }

    // ─── Kirim Pesan ──────────────────────────────────────────────────────────

    private fun kirimTeks() {
        val teks = binding.etPesan.text?.toString()?.trim()

        // Jika ada media yang dipilih, kirim media + teks (opsional)
        if (selectedMediaUri != null) {
            val caption = if (!teks.isNullOrEmpty()) teks else ""
            tambahPesan(
                ChatMessage(
                    pengirim = "driver",
                    isi      = selectedMediaUri.toString(),
                    tipe     = selectedMediaType
                )
            )
            if (caption.isNotEmpty()) {
                tambahPesan(ChatMessage(pengirim = "driver", isi = caption))
            }
            resetMediaPreview()
            binding.etPesan.setText("")
            // TODO: upload media ke server & kirim via API/Firestore
            return
        }

        if (teks.isNullOrEmpty()) return
        tambahPesan(ChatMessage(pengirim = "driver", isi = teks))
        binding.etPesan.setText("")
        // TODO: kirim teks via API/Firestore
    }

    private fun tambahPesan(pesan: ChatMessage) {
        pesanList.add(pesan)
        chatAdapter.notifyItemInserted(pesanList.lastIndex)
        binding.rvChat.scrollToPosition(pesanList.lastIndex)
    }

    // ─── Media ────────────────────────────────────────────────────────────────

    private fun pilihSumberFoto() {
        // Tampilkan dialog pilih: Kamera atau Galeri
        val opsi = arrayOf("📷 Ambil Foto", "🖼️ Pilih dari Galeri")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Kirim Foto")
            .setItems(opsi) { _, which ->
                when (which) {
                    0 -> {
                        // Kamera
                        val izinKamera = Manifest.permission.CAMERA
                        if (ActivityCompat.checkSelfPermission(requireContext(), izinKamera)
                            == PackageManager.PERMISSION_GRANTED
                        ) bukaKamera()
                        else launcherIzinKamera.launch(izinKamera)
                    }
                    1 -> launcherGaleriFoto.launch("image/*")
                }
            }.show()
    }

    private fun bukaKamera() {
        val fotoFile = File.createTempFile(
            "chat_foto_${System.currentTimeMillis()}", ".jpg",
            requireContext().cacheDir
        )
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            fotoFile
        )
        launcherKameraFoto.launch(photoUri)
    }

    private fun tampilkanPreviewMedia(uri: Uri, tipe: TipePesan, label: String) {
        selectedMediaUri  = uri
        selectedMediaType = tipe
        with(binding) {
            layoutMediaPreview.visibility = View.VISIBLE
            tvMediaLabel.text = label
            if (tipe == TipePesan.GAMBAR) {
                ivMediaPreview.setImageURI(uri)
            } else {
                // Untuk video tampilkan ikon generik
                ivMediaPreview.setImageResource(R.drawable.ic_videocam)
            }
        }
    }

    private fun resetMediaPreview() {
        selectedMediaUri = null
        binding.layoutMediaPreview.visibility = View.GONE
        binding.ivMediaPreview.setImageDrawable(null)
    }

    // ─── VoIP Panggilan ───────────────────────────────────────────────────────

    /**
     * Memulai panggilan VoIP dalam aplikasi.
     *
     * Implementasi lengkap VoIP memerlukan server signaling (WebRTC) atau SDK pihak ketiga
     * seperti Agora, Twilio Voice, atau Jitsi Meet SDK.
     *
     * Pada tahap ini, fragment VoIP call screen dibuka. Integrasi SDK WebRTC/Agora
     * dilakukan di dalam VoipCallFragment berdasarkan kebutuhan project.
     *
     * Jika nomorPembeli tersedia dan VoIP SDK belum siap, fallback ke dial biasa.
     */
    private fun mulaiPanggilan() {
        if (nomorPembeli.isBlank()) {
            Toast.makeText(requireContext(), "Nomor tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(requireContext(), "Menghubungi $namaPembeli...", Toast.LENGTH_SHORT).show()
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$nomorPembeli")))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}