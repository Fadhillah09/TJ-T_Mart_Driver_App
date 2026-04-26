package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.MainActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ActivityIntroBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.auth.LoginActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek Session
        val session = SessionManager(this)
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // View Binding
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Klik tombol Mulai dari XML
        binding.btnMulai.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}