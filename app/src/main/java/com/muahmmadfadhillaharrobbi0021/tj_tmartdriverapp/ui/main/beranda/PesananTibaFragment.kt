package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentPesananTibaBinding
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.beranda.BerandaFragment
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

class PesananTibaFragment : Fragment() {

    private var _binding: FragmentPesananTibaBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val ARG_NAMA = "nama_pembeli"
        private const val ARG_TOTAL = "total_harga"

        fun newInstance(namaPembeli: String, totalHarga: Int) =
            PesananTibaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAMA, namaPembeli)
                    putInt(ARG_TOTAL, totalHarga)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPesananTibaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nama = arguments?.getString(ARG_NAMA) ?: "Pelanggan"
        val total = arguments?.getInt(ARG_TOTAL) ?: 0

        val nf = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }

        binding.tvNamaPembeliSelesai.text = nama
        binding.tvTotalSelesai.text = nf.format(total)

        // Mulai semua animasi berurutan
        startAnimations()

        binding.btnKembaliKeBeranda.setOnClickListener {
            navigateToBeranda()
        }
    }

    private fun startAnimations() {
        // ── 1. Pulse circles (langsung mulai) ─────────────────────────
        startPulseAnimation()

        // ── 2. Lingkaran putih muncul dengan bounce (delay 200ms) ──────
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.circleBackground, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(binding.circleBackground, "scaleY", 0f, 1f)
                )
                duration = 500
                interpolator = OvershootInterpolator(3f)
                start()
            }
        }, 200)

        // ── 3. Icon centang muncul (delay 550ms) ───────────────────────
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(binding.ivCheckIcon, "scaleX", 0f, 1f),
                    ObjectAnimator.ofFloat(binding.ivCheckIcon, "scaleY", 0f, 1f)
                )
                duration = 400
                interpolator = BounceInterpolator()
                start()
            }
        }, 550)

        // ── 4. Teks "Pesanan Tiba!" slide up + fade in (delay 800ms) ──
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            animateTextIn(binding.tvSelesai, 0)
            animateTextIn(binding.tvSubSelesai, 100)
            animateTextIn(binding.tvNamaPembeliSelesai, 200)
        }, 800)

        // ── 5. Divider + info total (delay 1200ms) ────────────────────
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            animateFadeIn(binding.dividerSelesai, 300)
            animateFadeIn(binding.layoutInfoTotal, 450)
        }, 1200)

        // ── 6. Tombol beranda (delay 1600ms) ──────────────────────────
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            animateFadeIn(binding.btnKembaliKeBeranda, 0)
        }, 1600)

        // ── 7. Confetti burst (delay 400ms) ───────────────────────────
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            launchConfetti()
        }, 400)
    }

    private fun animateTextIn(view: View, extraDelay: Long) {
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(view, "translationY", 30f, 0f)
                )
                duration = 400
                interpolator = DecelerateInterpolator()
                start()
            }
        }, extraDelay)
    }

    private fun animateFadeIn(view: View, extraDelay: Long) {
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 400
                start()
            }
        }, extraDelay)
    }

    /**
     * Pulse circle: dua lingkaran transparan yang melebar dan memudar
     * memberikan efek "gelombang" dari tengah layar.
     */
    private fun startPulseAnimation() {
        fun pulse(view: View, delay: Long) {
            val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.3f, 1f).apply {
                duration = 1200; startDelay = delay
                repeatCount = ObjectAnimator.INFINITE; repeatMode = ValueAnimator.RESTART
            }
            val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.3f, 1f).apply {
                duration = 1200; startDelay = delay
                repeatCount = ObjectAnimator.INFINITE; repeatMode = ValueAnimator.RESTART
            }
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0.35f, 0f).apply {
                duration = 1200; startDelay = delay
                repeatCount = ObjectAnimator.INFINITE; repeatMode = ValueAnimator.RESTART
            }
            AnimatorSet().apply { playTogether(scaleX, scaleY, alpha); start() }
        }
        pulse(binding.pulseCircle1, 0L)
        pulse(binding.pulseCircle2, 600L)
    }

    /**
     * Confetti: buat View-view kecil berwarna-warni yang jatuh dari atas.
     * Tidak butuh library eksternal — semua pakai ObjectAnimator.
     */
    private fun launchConfetti() {
        val container = binding.confettiContainer
        val colors = listOf(
            "#FFFFFF", "#FFCDD2", "#FFD700", "#FF8A65",
            "#80CBC4", "#CE93D8", "#A5D6A7", "#81D4FA"
        )
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        repeat(60) { i ->
            val size = Random.nextInt(8, 20)
            val dot = View(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    leftMargin = Random.nextInt(0, screenWidth)
                    topMargin = -size
                }
                setBackgroundColor(Color.parseColor(colors[Random.nextInt(colors.size)]))
                rotation = Random.nextFloat() * 360f
                alpha = Random.nextFloat() * 0.5f + 0.5f
            }
            container.addView(dot)

            val startDelay = (i * 30L) + Random.nextLong(0, 300)
            val duration = Random.nextLong(1200, 2500)
            val endY = screenHeight + size.toFloat()

            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(dot, "translationY", -size.toFloat(), endY),
                    ObjectAnimator.ofFloat(dot, "rotation", dot.rotation, dot.rotation + Random.nextFloat() * 720f),
                    ObjectAnimator.ofFloat(dot, "alpha", dot.alpha, 0f)
                )
                this.duration = duration
                this.startDelay = startDelay
                interpolator = DecelerateInterpolator(0.8f)
                start()
            }

            // Bersihkan view setelah animasi selesai
            handler.postDelayed({
                if (_binding != null) container.removeView(dot)
            }, startDelay + duration + 100)
        }
    }

    private fun navigateToBeranda() {
        // Bersihkan seluruh back stack lalu load BerandaFragment
        parentFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, BerandaFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}