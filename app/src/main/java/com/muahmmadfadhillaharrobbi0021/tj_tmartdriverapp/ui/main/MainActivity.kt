package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ActivityMainBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.MessageResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.auth.LoginActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Cek login, kalau belum login tendang ke halaman Login
        if (!sessionManager.isLoggedIn()) {
            goToLogin()
            return
        }

        // Default fragment saat pertama buka = Beranda
        // loadFragment(BerandaFragment()) // Aktifkan ini setelah file BerandaFragment dibuat

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var fragment: Fragment? = null

            when (item.itemId) {
                R.id.nav_beranda -> {
                    // fragment = BerandaFragment()
                }
                R.id.nav_omset -> {
                    // fragment = OmsetFragment()
                }
                R.id.nav_riwayat -> {
                    // fragment = RiwayatFragment()
                }
                R.id.nav_pengaturan -> {
                    // fragment = PengaturanFragment()
                }
            }

            fragment?.let {
                loadFragment(it)
                true
            } ?: false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * Fungsi Logout: Hapus token di Laravel dan hapus data di HP
     */
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