package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.pengaturan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentPengaturanBinding

class PengaturanFragment : Fragment() {

    private var _binding: FragmentPengaturanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengaturanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Tombol Back ke Beranda
        binding.btnBack.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
                ?.selectedItemId = R.id.nav_beranda
        }

        // 2. Menu Tentang Aplikasi
        binding.menuTentang.setOnClickListener {
            navigateTo(TentangAplikasiFragment())
        }

        // 3. Menu Panduan Pengguna
        binding.menuPanduan.setOnClickListener {
            navigateTo(PanduanPenggunaFragment())
        }

        // 4. Menu Kebijakan & Privasi
        binding.menuKebijakan.setOnClickListener {
            navigateTo(KebijakanPrivasiFragment())
        }

        // 5. Menu Hubungi Kami
        binding.menuHubungi.setOnClickListener {
            navigateTo(HubungiKamiFragment())
        }
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}