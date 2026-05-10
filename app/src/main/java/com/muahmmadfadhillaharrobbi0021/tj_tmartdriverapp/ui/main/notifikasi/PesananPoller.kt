package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.api.ApiClient
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.model.PesananResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PesananPoller(
    private val context: Context,
    private val bearerToken: String,
    private val onPesananBaru: (pesananId: Int) -> Unit
) {
    companion object {
        private const val INTERVAL_MS = 10_000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val knownIds = mutableSetOf<Int>()
    private var isRunning = false
    private var isFirstRun = true

    fun start() {
        if (isRunning) return
        isRunning = true

        // Isi knownIds dari notifikasi yang sudah tersimpan di storage
        val savedNotifs = NotifikasiStorage.load(context)
        if (savedNotifs.isNotEmpty()) {
            savedNotifs.mapNotNull { it.pesananId }.forEach { knownIds.add(it) }
            // Sudah ada data lama — poll pertama langsung deteksi pesanan baru
            isFirstRun = false
        }
        // Kalau storage kosong (pertama kali install),
        // isFirstRun tetap true → poll pertama hanya catat ID lama

        handler.post(pollRunnable)
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(pollRunnable)
    }

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            poll()
            handler.postDelayed(this, INTERVAL_MS)
        }
    }

    private fun poll() {
        ApiClient.instance.getPesanan(bearerToken)
            .enqueue(object : Callback<PesananResponse> {
                override fun onResponse(
                    call: Call<PesananResponse>,
                    response: Response<PesananResponse>
                ) {
                    if (!response.isSuccessful) return
                    val pesananList = response.body()?.data ?: return

                    if (isFirstRun) {
                        // Pertama kali install — catat semua ID yang sudah ada,
                        // jangan munculkan notifikasi
                        pesananList.forEach { pesanan ->
                            pesanan.id?.let { knownIds.add(it) }
                        }
                        isFirstRun = false
                        return
                    }

                    // Poll normal — cek ID baru
                    pesananList.forEach { pesanan ->
                        val id = pesanan.id ?: return@forEach
                        if (id !in knownIds) {
                            knownIds.add(id)

                            val alreadyExists = NotifikasiFragment.allNotifikasi
                                .any { it.pesananId == id }
                            if (!alreadyExists) {
                                val notif = NotifikasiItem(
                                    id = id,
                                    pesananId = id,
                                    title = "Pesanan Masuk",
                                    message = "Kesempatan order baru di dekatmu.",
                                    time = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date()),
                                    isRead = false,
                                    type = NotifType.PESANAN,
                                    timestamp = System.currentTimeMillis()
                                )
                                NotifikasiFragment.allNotifikasi.add(0, notif)

                                // Tampilkan in-app popup jika activity masih aktif
                                val activity = context as? Activity
                                if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                                    InAppNotificationPopup.show(
                                        activity = activity,
                                        title = notif.title,
                                        message = notif.message,
                                        pesananId = id
                                    )
                                }

                                onPesananBaru(id)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                    // Gagal — coba lagi di interval berikutnya
                }
            })
    }
}