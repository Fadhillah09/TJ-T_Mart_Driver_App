package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.profil

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentProfilBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.auth.LoginActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadPhotoFromUri(it) }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let { uploadPhotoFromBitmap(it) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnEditPhoto.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        loadUserData()
        loadProfileFromServer()
    }

    private fun loadUserData() {
        binding.tvNama.text   = "Nama : ${sessionManager.getUserName()}"
        binding.tvNoHp.text   = "No HP : ${sessionManager.getNoTelp()}"
        binding.tvEmail.text  = "Email : ${sessionManager.getEmail()}"
        binding.tvLokasi.text = "Lokasi : Bandung"
    }

    private fun loadProfileFromServer() {
        val token   = sessionManager.getToken() ?: return
        val baseUrl = sessionManager.getBaseUrl()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client  = OkHttpClient()
                val request = Request.Builder()
                    .url("$baseUrl/api/driver/profile")
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val body     = response.body?.string() ?: return@launch

                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val data = json.getJSONObject("data")

                    // 🔥 DEBUG (lihat di Logcat)
                    Log.d("PROFILE_DATA", data.toString())

                    val fotoUrl  = if (data.isNull("foto_url")) null else data.getString("foto_url")
                    val namaBank = if (data.isNull("nama_bank")) "-" else data.getString("nama_bank")

                    // 🔥 FLEXIBLE ambil nomor rekening
                    val noRek = when {
                        data.has("no_rekening") -> data.getString("no_rekening")
                        data.has("no_rek") -> data.getString("no_rek")
                        data.has("rekening") -> data.getString("rekening")
                        data.has("nomor_rekening") -> data.getString("nomor_rekening")
                        else -> "-"
                    }

                    withContext(Dispatchers.Main) {

                        if (!fotoUrl.isNullOrEmpty()) {
                            sessionManager.saveFotoProfil(fotoUrl) // ← TAMBAHAN 1
                            Glide.with(requireContext())
                                .load(fotoUrl)
                                .placeholder(R.drawable.ic_back)
                                .circleCrop()
                                .into(binding.imgProfile)
                        }

                        binding.tvNamaBank.text = "Nama Bank : $namaBank"
                        binding.tvNomorRek.text = "No rekening : $noRek"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Kamera", "Galeri")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Foto Profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(null)
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun uploadPhotoFromUri(uri: Uri) {
        binding.imgProfile.setImageURI(uri)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = uriToFile(uri)
                uploadToServer(file)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal memproses foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadPhotoFromBitmap(bitmap: Bitmap) {
        binding.imgProfile.setImageBitmap(bitmap)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val file = bitmapToFile(bitmap)
                uploadToServer(file)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal memproses foto", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun uploadToServer(file: File) {
        val token   = sessionManager.getToken() ?: return
        val baseUrl = sessionManager.getBaseUrl()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "foto",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("$baseUrl/api/driver/upload-photo")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .build()

        val client   = OkHttpClient()
        val response = client.newCall(request).execute()
        val body     = response.body?.string() ?: ""

        withContext(Dispatchers.Main) {
            if (response.isSuccessful) {
                val json    = JSONObject(body)
                val fotoUrl = json.getString("foto_url")
                sessionManager.saveFotoProfil(fotoUrl) // ← TAMBAHAN 2
                Glide.with(requireContext())
                    .load(fotoUrl)
                    .circleCrop()
                    .into(binding.imgProfile)

                Toast.makeText(requireContext(), "Foto profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Gagal upload foto", Toast.LENGTH_SHORT).show()
            }
        }

        file.delete()
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)!!
        val file = File(requireContext().cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out -> inputStream.copyTo(out) }
        return file
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File(requireContext().cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}