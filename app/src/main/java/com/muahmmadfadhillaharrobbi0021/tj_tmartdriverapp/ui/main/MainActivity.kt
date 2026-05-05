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

        // ALUR PERBAIKAN: Klik tombol langsung memicu kamera
        binding.btnAbsensi.setOnClickListener {
            bukakKamera()
        }
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

        // Validasi dilakukan SETELAH foto diambil
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            checkLocationAndTimeAfterScan()
        }
    }

    private fun checkLocationAndTimeAfterScan() {
        // 1. VALIDASI WAKTU (06:30 - 08:00 WIB Jakarta)
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)

        val totalMinutes = (hour * 60) + minute
        val startMinute = (6 * 60) + 30 // Jam 06:30
        val endMinute = 8 * 60         // Jam 08:00

        val isPagi = totalMinutes in startMinute..endMinute
        // Jika ingin menambah absen sore, gunakan: val isSore = (hour >= 15)

        if (!isPagi) {
            val message = if (totalMinutes < startMinute)
                "Absensi belum dibuka. Mulai jam 06:30 WIB."
            else
                "Absensi ditolak! Batas waktu maksimal adalah 08:00 WIB."

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            return // Stop proses jika waktu tidak sesuai
        }

        // 2. CEK IZIN LOKASI
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        // 3. VALIDASI JARAK RADIUS (Pesan "Di luar jangkauan" muncul di sini)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(TARGET_LAT, TARGET_LNG, location.latitude, location.longitude, results)
                    val distance = results[0]

                    if (distance <= MAX_RADIUS) {
                        // Lolos validasi waktu & lokasi
                        Toast.makeText(this, "Absensi Berhasil dikirim!", Toast.LENGTH_SHORT).show()
                        // Panggil fungsi kirim data ke API Laravel tubes_pbw2 di sini
                    } else {
                        // Notifikasi jarak baru muncul SEKARANG setelah scan
                        Toast.makeText(this, "Gagal! Anda di luar jangkauan Telkom! Jarak: ${distance.toInt()}m", Toast.LENGTH_LONG).show()
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
                // Jika izin baru diberikan, ulangi pengecekan setelah scan
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
}