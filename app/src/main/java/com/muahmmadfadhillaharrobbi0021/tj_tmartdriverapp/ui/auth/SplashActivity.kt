package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.ActivitySplashBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.MainActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            if (session.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2000)
    }
}