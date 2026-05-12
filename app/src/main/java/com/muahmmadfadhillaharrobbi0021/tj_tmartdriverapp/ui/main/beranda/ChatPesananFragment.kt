package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private var photoUri: Uri? = null
    private var selectedMediaUri: Uri? = null
    private var selectedMediaType: TipePesan = TipePesan.GAMBAR

    // ── Activity Result Launchers ──────────────────────────────────────────────

    private val launcherKameraFoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { berhasil ->
        if (berhasil && photoUri != null)
            tampilkanPreviewMedia(photoUri!!, TipePesan.GAMBAR, "1 foto siap dikirim")
    }

    private val launcherGaleriFoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { tampilkanPreviewMedia(it, TipePesan.GAMBAR, "1 foto dipilih") }
    }

    private val launcherGaleriVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { tampilkanPreviewMedia(it, TipePesan.VIDEO, "1 video dipilih") }
    }

    private val launcherIzinKamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) bukaKamera()
        else Toast.makeText(requireContext(), "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
    }

    private val launcherIzinTelepon = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.RECORD_AUDIO] == true) mulaiPanggilan()
        else Toast.makeText(
            requireContext(),
            "Izin mikrofon diperlukan untuk panggilan",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ─── Companion ────────────────────────────────────────────────────────────

    companion object {
        private const val ARG_PESANAN_ID = "pesanan_id"
        private const val ARG_NAMA = "nama_pembeli"
        private const val ARG_NOMOR = "nomor_pembeli"
        private const val ARG_AUTO_PESAN = "auto_pesan"
        private const val ARG_FOTO       = "foto_pembeli"

        fun newInstance(
            pesananId: Int,
            namaPembeli: String,
            nomorPembeli: String,
            autoPesan: String?,
            fotoPembeli: String? = null
        ) = ChatPesananFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PESANAN_ID, pesananId)
                putString(ARG_NAMA, namaPembeli)
                putString(ARG_NOMOR, nomorPembeli)
                putString(ARG_AUTO_PESAN, autoPesan)
                putString(ARG_FOTO, fotoPembeli)
            }
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    private var fotoPembeli: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pesananId = it.getInt(ARG_PESANAN_ID)
            namaPembeli = it.getString(ARG_NAMA, "Pelanggan")
            nomorPembeli = it.getString(ARG_NOMOR, "")
            autoPesan = it.getString(ARG_AUTO_PESAN)
            fotoPembeli = it.getString(ARG_FOTO)
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

        autoPesan?.let { pesan ->
            tambahPesan(ChatMessage(pengirim = "driver", isi = pesan, tipe = TipePesan.TEKS))
            // TODO: kirim juga ke backend / Firestore
        }
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

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
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun setupInputListeners() {

        binding.etPesan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val sedangMenulis = !s.isNullOrEmpty()
                val visibility = if (sedangMenulis) View.GONE else View.VISIBLE
                binding.btnKamera.visibility = visibility
                binding.btnVoiceNote.visibility = visibility
            }
        })

        // ── PERUBAHAN 2: btnKamera (gabungan foto+video) via popup ────────────
        binding.btnKamera.setOnClickListener { tampilkanPopupKamera() }

        // ── PERUBAHAN 3: Voice Note ───────────────────────────────────────────
        binding.btnVoiceNote.setOnClickListener {
            // TODO: implementasi rekam voice note
            Toast.makeText(requireContext(), "Tahan untuk rekam VN", Toast.LENGTH_SHORT).show()
        }

        // Kirim pesan teks / media
        binding.btnKirimPesan.setOnClickListener { kirimTeks() }

        // Batal preview media
        binding.btnBatalMedia.setOnClickListener { resetMediaPreview() }
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
            tambahPesan(
                ChatMessage(
                    pengirim = "driver",
                    isi = selectedMediaUri.toString(),
                    tipe = selectedMediaType
                )
            )
            if (!teks.isNullOrEmpty()) {
                tambahPesan(ChatMessage(pengirim = "driver", isi = teks))
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
        binding.rvChat.post {                                  // ← jadi begini
            binding.rvChat.scrollToPosition(pesanList.lastIndex)
        }
    }

    private fun pilihSumberFoto() {
        val opsi = arrayOf("📷 Ambil Foto", "🖼️ Pilih dari Galeri")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Kirim Foto")
            .setItems(opsi) { _, which ->
                when (which) {
                    0 -> {
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
        selectedMediaUri = uri
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
        Toast.makeText(requireContext(), "Menghubungi $namaPembeli...", Toast.LENGTH_SHORT).show()
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$nomorPembeli")))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}