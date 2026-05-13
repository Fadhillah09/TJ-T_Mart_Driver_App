package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.pengaturan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.R
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentTentangAplikasiBinding

class TentangAplikasiFragment : Fragment() {

    private var _binding: FragmentTentangAplikasiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTentangAplikasiBinding.inflate(inflater, container, false)

        // tombol back
        binding.btnBack.setOnClickListener {

            activity?.findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )?.selectedItemId = R.id.nav_pengaturan

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}