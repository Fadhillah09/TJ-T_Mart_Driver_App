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

    // --- TAMBAHKAN FUNGSI BARU DI SINI ---

    fun getPesananHariIni(): Int {
        return pref.getInt("pesanan_hari_ini", 0)
    }

    fun tambahPesananHariIni() {
        val currentCount = getPesananHariIni()
        editor.putInt("pesanan_hari_ini", currentCount + 1)
        editor.apply()
    }

    // -------------------------------------

    fun rejectPesananLokal(pesananId: Int) {
        val current = pref.getStringSet("rejected_orders", emptySet()) ?: emptySet()
        val rejectedIds = HashSet<String>(current)
        rejectedIds.add(pesananId.toString())
        pref.edit().putStringSet("rejected_orders", rejectedIds).apply()
    }

    fun getRejectedPesananIds(): Set<String> {
        return pref.getStringSet("rejected_orders", emptySet()) ?: emptySet()
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}