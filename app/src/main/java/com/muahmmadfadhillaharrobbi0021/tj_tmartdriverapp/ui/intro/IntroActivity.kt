package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ActivityIntroBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.auth.LoginActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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