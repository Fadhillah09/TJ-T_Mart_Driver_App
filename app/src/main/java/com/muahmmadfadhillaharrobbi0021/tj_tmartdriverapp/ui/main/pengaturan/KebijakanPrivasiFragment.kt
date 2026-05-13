package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.pengaturan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentKebijakanPrivasiBinding

class KebijakanPrivasiFragment : Fragment() {

    private var _binding: FragmentKebijakanPrivasiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKebijakanPrivasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}