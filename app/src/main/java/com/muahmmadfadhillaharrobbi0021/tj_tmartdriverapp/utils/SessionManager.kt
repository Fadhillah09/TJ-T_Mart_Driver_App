package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    fun saveSession(token: String, userId: Int, name: String, email: String, noTelp: String?) {
        editor.putBoolean(Constants.KEY_IS_LOGIN, true)
        editor.putString(Constants.KEY_TOKEN, token)
        editor.putInt(Constants.KEY_USER_ID, userId)
        editor.putString(Constants.KEY_USER_NAME, name)
        editor.putString(Constants.KEY_USER_EMAIL, email)
        editor.putString(Constants.KEY_USER_TELP, noTelp ?: "")
        editor.apply()
    }

    fun isLoggedIn(): Boolean = pref.getBoolean(Constants.KEY_IS_LOGIN, false)
    fun getToken(): String = pref.getString(Constants.KEY_TOKEN, "") ?: ""
    fun getUserId(): Int = pref.getInt(Constants.KEY_USER_ID, 0)
    fun getUserName(): String = pref.getString(Constants.KEY_USER_NAME, "Driver") ?: "Driver"
    fun getEmail(): String = pref.getString(Constants.KEY_USER_EMAIL, "") ?: ""
    fun getNoTelp(): String = pref.getString(Constants.KEY_USER_TELP, "") ?: ""

    fun getBearerToken(): String = "Bearer ${getToken()}"

    fun rejectPesananLokal(pesananId: Int) {
        val rejectedIds = getRejectedPesananIds().toMutableSet()
        rejectedIds.add(pesananId.toString())
        // Ganti 'sharedPreferences' jadi 'pref' atau nama variabel yang ada di atas
        pref.edit().putStringSet("rejected_orders", rejectedIds).apply()
    }

    fun getRejectedPesananIds(): Set<String> {
        // Ganti 'sharedPreferences' jadi 'pref' atau nama variabel yang ada di atas
        return pref.getStringSet("rejected_orders", emptySet()) ?: emptySet()
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}