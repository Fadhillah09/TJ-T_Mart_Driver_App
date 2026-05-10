package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ActivityMainBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.MessageResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.auth.LoginActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda.BerandaFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.InAppNotificationPopup
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.NotifikasiFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.NotifikasiItem
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.NotifikasiStorage
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.NotifType
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi.PesananPoller
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.omset.OmsetFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.pengaturan.PengaturanFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.riwayat.RiwayatFragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var pesananPoller: PesananPoller

    // Koordinat Telkom University Bojongsoang
    private val TARGET_LAT = -6.971306
    private val TARGET_LNG = 107.628611
    private val MAX_RADIUS = 10.0 // Batas 10 Meter

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_LOCATION_PERMISSION = 102
        private const val REQUEST_IMAGE_CAPTURE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        loadFragment(BerandaFragment())
        startAbsensiPulseAnimation()
        startGlobalPoller()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_beranda    -> BerandaFragment()
                R.id.nav_omset      -> OmsetFragment()
                R.id.nav_riwayat    -> RiwayatFragment()
                R.id.nav_pengaturan -> PengaturanFragment()
                else -> return@setOnItemSelectedListener false
            }
            loadFragment(fragment)
            true
        }

        binding.btnAbsensi.setOnClickListener {
            bukakKamera()
        }
    }

    private fun startGlobalPoller() {
        // Load notifikasi dari storage ke allNotifikasi jika belum ada
        if (NotifikasiFragment.allNotifikasi.isEmpty()) {
            val saved = NotifikasiStorage.load(this)
            val sorted = saved.sortedByDescending { it.timestamp }
            NotifikasiFragment.allNotifikasi.addAll(sorted)
        }

        pesananPoller = PesananPoller(
            context = this,
            bearerToken = sessionManager.getBearerToken(),
            onPesananBaru = { pesananId ->
                // Simpan ke storage setiap ada pesanan baru
                NotifikasiStorage.save(this, NotifikasiFragment.allNotifikasi)
            }
        )
        pesananPoller.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::pesananPoller.isInitialized) {
            pesananPoller.stop()
        }
        InAppNotificationPopup.dismiss(this)
    }

    private fun bukakKamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
                val intent = Intent("android.media.action.STILL_IMAGE_CAMERA")
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            checkLocationAndTimeAfterScan()
        }
    }

    private fun checkLocationAndTimeAfterScan() {

        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        val totalMinutes = (hour * 60) + minute
        val startPagi = (6 * 60) + 30
        val endPagi = 8 * 60
        val startSore = 15 * 60

        val isPagi = totalMinutes in startPagi..endPagi
        val isSore = totalMinutes >= startSore

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(TARGET_LAT, TARGET_LNG, location.latitude, location.longitude, results)
                    val distance = results[0]

                    if (distance <= MAX_RADIUS) {
                        tentukanKirimAbsen(location.latitude, location.longitude)
                        Toast.makeText(this, "Absensi Berhasil dikirim!", Toast.LENGTH_SHORT).show()
                    } else {
                        showAbsensiDialog("Gagal", "Anda di luar jangkauan Telkom! Jarak: ${distance.toInt()}m", false)
                    }
                } else {
                    Toast.makeText(this, "Gagal verifikasi lokasi. Pastikan GPS aktif.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) bukakKamera()
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) checkLocationAndTimeAfterScan()
            }
        }
    }

    private fun startAbsensiPulseAnimation() {
        fun animateRing(view: android.view.View, startDelay: Long) {
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.6f).apply {
                duration = 1800
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                this.startDelay = startDelay
            }
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.6f).apply {
                duration = 1800
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                this.startDelay = startDelay
            }
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0.6f, 0f).apply {
                duration = 1800
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                this.startDelay = startDelay
            }
            AnimatorSet().apply { playTogether(scaleX, scaleY, alpha); start() }
        }
        animateRing(binding.ringOuter, 0L)
        animateRing(binding.ringInner, 900L)
    }

    private fun kirimDataAbsensi(lat: Double, lng: Double, tipe: String) {
        val koordinat = "$lat,$lng"
        val token = sessionManager.getBearerToken()

        ApiClient.instance.prosesAbsen(token, koordinat).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    if (body.status == "success"
                        || body.message?.contains("sudah absen", ignoreCase = true) == true
                        || body.message?.contains("berhasil", ignoreCase = true) == true) {
                        sessionManager.setHasAbsenToday(true)
                    }
                    val judul = if (body.status == "success") "Berhasil" else "Perhatian"
                    showAbsensiDialog(judul, body.message ?: "Proses absensi selesai", body.status == "success")
                } else {
                    val errorJson = response.errorBody()?.string()
                    showAbsensiDialog("Gagal", "Info: $errorJson", false)
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                showAbsensiDialog("Error", "Koneksi Bermasalah", false)
            }
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    fun doLogout() {
        val token = sessionManager.getBearerToken()
        ApiClient.instance.logout(token).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) { clearAndGoLogin() }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) { clearAndGoLogin() }
        })
    }

    private fun clearAndGoLogin() {
        sessionManager.logout()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showAbsensiDialog(title: String, message: String, isSuccess: Boolean) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message ?: "Tidak ada pesan dari server")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun tentukanKirimAbsen(lat: Double, lng: Double) {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)

        if (hour >= 7) {
            kirimDataAbsensi(lat, lng, "masuk")
        } else {
            showAbsensiDialog("Gagal", "Sesi absensi belum dibuka. Silakan kembali jam 07:00 WIB.", false)
        }
    }
}