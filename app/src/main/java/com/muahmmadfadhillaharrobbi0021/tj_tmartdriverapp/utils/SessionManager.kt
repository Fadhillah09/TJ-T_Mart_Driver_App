package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val pref: SharedPreferences =
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    fun saveSession(
        token: String,
        userId: Int,
        name: String,
        email: String,
        noTelp: String?,
        nomorRekening: String?
    ) {
        editor.putBoolean(Constants.KEY_IS_LOGIN, true)
        editor.putString(Constants.KEY_TOKEN, token)
        editor.putInt(Constants.KEY_USER_ID, userId)
        editor.putString(Constants.KEY_USER_NAME, name)
        editor.putString(Constants.KEY_USER_EMAIL, email)
        editor.putString(Constants.KEY_USER_TELP, noTelp ?: "")
        editor.putString(Constants.KEY_USER_REKENING, nomorRekening ?: "")
        editor.apply()
    }

    fun isLoggedIn(): Boolean = pref.getBoolean(Constants.KEY_IS_LOGIN, false)
    fun getToken(): String = pref.getString(Constants.KEY_TOKEN, "") ?: ""
    fun getUserId(): Int = pref.getInt(Constants.KEY_USER_ID, 0)
    fun getUserName(): String = pref.getString(Constants.KEY_USER_NAME, "Driver") ?: "Driver"
    fun getEmail(): String = pref.getString(Constants.KEY_USER_EMAIL, "") ?: ""
    fun getNoTelp(): String = pref.getString(Constants.KEY_USER_TELP, "") ?: ""
    fun getNoRekening(): String = pref.getString(Constants.KEY_USER_REKENING, "") ?: ""

    fun getBearerToken(): String = "Bearer ${getToken()}"

    fun getBaseUrl(): String = Constants.BASE_URL.removeSuffix("/api/")

    fun saveFotoProfil(url: String) {
        editor.putString("foto_profil", url)
        editor.apply()
    }

    fun getFotoProfil(): String = pref.getString("foto_profil", "") ?: ""

    fun rejectPesananLokal(pesananId: Int) {
        val current = pref.getStringSet("rejected_orders", emptySet()) ?: emptySet()
        val rejectedIds = HashSet<String>(current)
        rejectedIds.add(pesananId.toString())
        pref.edit().putStringSet("rejected_orders", rejectedIds).apply()
    }

    fun getRejectedPesananIds(): Set<String> {
        return pref.getStringSet("rejected_orders", emptySet()) ?: emptySet()
    }

    // ─── PESANAN HARIAN ────────────────────────────────────────────────────────

    private val KEY_PESANAN_HARIAN_COUNT = "pesanan_harian_count"
    private val KEY_PESANAN_HARIAN_TANGGAL = "pesanan_harian_tanggal"

    /** Ambil tanggal hari ini dalam format "yyyy-MM-dd" */
    private fun getTodayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    /**
     * Ambil jumlah pesanan hari ini.
     * Otomatis reset ke 0 kalau tanggal sudah berganti.
     */
    fun getPesananHariIni(): Int {
        val savedDate = pref.getString(KEY_PESANAN_HARIAN_TANGGAL, "") ?: ""
        val today = getTodayString()
        return if (savedDate == today) {
            pref.getInt(KEY_PESANAN_HARIAN_COUNT, 0)
        } else {
            // Hari sudah berganti → reset otomatis
            editor.putString(KEY_PESANAN_HARIAN_TANGGAL, today)
            editor.putInt(KEY_PESANAN_HARIAN_COUNT, 0)
            editor.apply()
            0
        }
    }

    /**
     * Tambah 1 ke counter pesanan hari ini.
     * Otomatis reset ke 1 kalau tanggal sudah berganti.
     */
    fun tambahPesananHariIni() {
        val today = getTodayString()
        val savedDate = pref.getString(KEY_PESANAN_HARIAN_TANGGAL, "") ?: ""
        val currentCount = if (savedDate == today) {
            pref.getInt(KEY_PESANAN_HARIAN_COUNT, 0)
        } else {
            0
        }
        editor.putString(KEY_PESANAN_HARIAN_TANGGAL, today)
        editor.putInt(KEY_PESANAN_HARIAN_COUNT, currentCount + 1)
        editor.apply()
    }

    // ──────────────────────────────────────────────────────────────────────────

    fun logout() {
        editor.clear()
        editor.apply()
    }
}