package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.utils

object Constants {
    // Ganti sesuai kebutuhan: true = development/emulator, false = production
    private const val IS_DEVELOPMENT = true

    private const val CLOUDFLARE_URL = "https://proud-base-e00d.fadhillaharrobbi.workers.dev/"
    private const val LOCAL_URL = "http://10.0.2.2:8000/"

    // Gunakan ini untuk mengambil foto (tanpa folder api)
    val BASE_URL = if (IS_DEVELOPMENT) LOCAL_URL else CLOUDFLARE_URL

    // Gunakan ini untuk request data (dengan folder api)
    val API_BASE_URL = if (IS_DEVELOPMENT) "${LOCAL_URL}api/" else "${CLOUDFLARE_URL}api/"

    // SharedPreferences
    const val PREF_NAME = "TJMartDriverPref"
    const val KEY_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_TELP = "user_telp"

    const val KEY_USER_REKENING = "user_rekening"
    const val KEY_IS_LOGIN = "is_logged_in"
}