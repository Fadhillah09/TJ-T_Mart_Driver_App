package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        // Default fragment
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

        // Tombol Absensi → buka kamera
        binding.btnAbsensi.setOnClickListener {
            bukakKamera()
        }
    }

    private fun bukakKamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } catch (e: Exception) {
                // Fallback: buka app kamera langsung
                try {
                    val intent = Intent("android.media.action.STILL_IMAGE_CAMERA")
                    startActivity(intent)
                } catch (e2: Exception) {
                    Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                bukakKamera()
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Foto absensi berhasil diambil!", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Animasi sonar merah pada tombol Absensi ───
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
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun doLogout() {
        val token = sessionManager.getBearerToken()
        ApiClient.instance.logout(token).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                clearAndGoLogin()
            }
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                clearAndGoLogin()
            }
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