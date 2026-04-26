package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.MainActivity
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.LoginRequest
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.LoginResponse
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password wajib diisi"
            return
        }

        setLoading(true)

        ApiClient.instance.loginDriver(LoginRequest(email, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    setLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        if (data.status == "success" && data.token != null) {
                            val user = data.user
                            sessionManager.saveSession(
                                data.token,
                                user?.id ?: 0,
                                user?.name ?: "",
                                user?.email ?: "",
                                user?.noTelp
                            )
                            goToMain()
                        } else {
                            showError("Login gagal. Coba lagi.")
                        }
                    } else {
                        when (response.code()) {
                            401 -> showError("Email atau Password salah")
                            403 -> showError("Anda tidak memiliki akses sebagai kurir.")
                            422 -> showError("Format email tidak valid")
                            else -> showError("Server error: ${response.code()}")
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    setLoading(false)
                    showError("Tidak bisa konek ke server. Cek koneksi Internet.")
                }
            })
    }

    private fun setLoading(loading: Boolean) {
        btnLogin.isEnabled = !loading
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.text = if (loading) "Memproses..." else "Login"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}