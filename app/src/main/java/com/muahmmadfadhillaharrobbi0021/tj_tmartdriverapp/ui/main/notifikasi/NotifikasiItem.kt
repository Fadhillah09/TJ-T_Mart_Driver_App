package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi

enum class NotifType {
    PESANAN,
    INFO
}

data class NotifikasiItem(
    val id: Int,
    val pesananId: Int? = null,      // ID pesanan di backend, untuk navigasi ke detail
    val title: String,
    val message: String,
    val time: String,
    var isRead: Boolean,
    val type: NotifType,
    val timestamp: Long = System.currentTimeMillis()
)