package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.notifikasi

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda.DetailPesananFragment

object InAppNotificationPopup {

    private const val AUTO_DISMISS_MS = 4000L
    private var currentPopup: View? = null
    private val handler = Handler(Looper.getMainLooper())

    fun show(
        activity: Activity,
        title: String,
        message: String,
        pesananId: Int
    ) {
        // Dismiss popup sebelumnya jika masih tampil
        dismiss(activity)

        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        val popup = LayoutInflater.from(activity)
            .inflate(R.layout.layout_inapp_notification, rootView, false)

        popup.findViewById<TextView>(R.id.tvPopupTitle).text = title
        popup.findViewById<TextView>(R.id.tvPopupMessage).text = message

        // Layout params — tempel di atas, full width, margin atas untuk status bar
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP
            topMargin = getStatusBarHeight(activity)
            leftMargin = 16.dpToPx(activity)
            rightMargin = 16.dpToPx(activity)
        }

        rootView.addView(popup, params)
        currentPopup = popup

        // Animasi masuk dari atas
        popup.translationY = -300f
        popup.alpha = 0f
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(popup, "translationY", -300f, 0f),
                ObjectAnimator.ofFloat(popup, "alpha", 0f, 1f)
            )
            duration = 350
            interpolator = DecelerateInterpolator()
            start()
        }

        // Klik popup → navigasi ke detail pesanan
        popup.setOnClickListener {
            dismiss(activity)
            navigateToDetail(activity, pesananId)
        }

        // Auto dismiss setelah 4 detik
        handler.postDelayed({
            dismiss(activity)
        }, AUTO_DISMISS_MS)
    }

    fun dismiss(activity: Activity) {
        val popup = currentPopup ?: return
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

        handler.removeCallbacksAndMessages(null)

        // Animasi keluar ke atas
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(popup, "translationY", 0f, -300f),
                ObjectAnimator.ofFloat(popup, "alpha", 1f, 0f)
            )
            duration = 250
            interpolator = DecelerateInterpolator()
            start()
        }

        handler.postDelayed({
            try { rootView.removeView(popup) } catch (e: Exception) { /* ignored */ }
        }, 260)

        currentPopup = null
    }

    private fun navigateToDetail(activity: Activity, pesananId: Int) {
        val fragmentManager = (activity as? androidx.fragment.app.FragmentActivity)
            ?.supportFragmentManager ?: return

        val detailFragment = DetailPesananFragment.newInstance(pesananId)
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, detailFragment)
            .addToBackStack("DetailPesanan")
            .commit()
    }

    private fun getStatusBarHeight(activity: Activity): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) activity.resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun Int.dpToPx(activity: Activity): Int {
        return (this * activity.resources.displayMetrics.density).toInt()
    }
}