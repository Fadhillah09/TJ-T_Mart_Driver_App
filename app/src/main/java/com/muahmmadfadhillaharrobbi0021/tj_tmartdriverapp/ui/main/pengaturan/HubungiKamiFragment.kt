package com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.ui.main.pengaturan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.muahmmadfadhillaharrobbi0021.tj_tmartdriverapp.databinding.FragmentHubungiKamiBinding

class HubungiKamiFragment : Fragment() {

    private var _binding: FragmentHubungiKamiBinding? = null
    private val binding get() = _binding!!

    private val nomorTelpon = "082258779970"
    private val emailDukungan = "tjtmart@gmail.com"
    private val nomorWhatsApp = "6282258779970"
    private val latKantor = -6.9712748
    private val lngKantor = 107.6286468
    private val namaKantor = "TJ Mart Telkom University"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHubungiKamiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Telpon CS → buka aplikasi telepon
        binding.itemTelpon.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$nomorTelpon")
            })
        }

        // Email → buka aplikasi email
        binding.itemEmail.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$emailDukungan")
            })
        }

        // WhatsApp → buka chat WhatsApp langsung
        binding.itemWhatsApp.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$nomorWhatsApp")
                    setPackage("com.whatsapp")
                })
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$nomorWhatsApp")
                })
            }
        }

        // Kantor → buka Google Maps dengan koordinat spesifik
        binding.itemKantor.setOnClickListener {
            val mapsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:$latKantor,$lngKantor?q=$latKantor,$lngKantor($namaKantor)")
                setPackage("com.google.android.apps.maps")
            }
            if (mapsIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(mapsIntent)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://maps.google.com/?q=$latKantor,$lngKantor")
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}