package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.omset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentOmsetBinding

class OmsetFragment : Fragment() {

    private var _binding: FragmentOmsetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOmsetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}