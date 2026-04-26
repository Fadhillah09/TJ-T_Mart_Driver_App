package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils

object Constants {
    // BASE_URL — sesuaikan dengan environment:
    // Emulator Android Studio  → "http://10.0.2.2:8000/api/"
    // HP Fisik (WiFi sama PC)  → "http://192.168.X.X:8000/api/"
    const val BASE_URL = "http://10.0.2.2:8000/api/"

    // SharedPreferences
    const val PREF_NAME = "TJMartDriverPref"
    const val KEY_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_TELP = "user_telp"
    const val KEY_IS_LOGIN = "is_logged_in"
}