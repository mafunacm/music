package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.musicplayer.databinding.FragmentSettingsBinding

import androidx.media3.common.util.UnstableApi

@UnstableApi
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnEq.setOnClickListener {
            EqualizerFragment.newInstance().show(parentFragmentManager, "equalizer")
        }
        binding.btnTheme.setOnClickListener {
            Toast.makeText(requireContext(), "Theme settings coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.btnAbout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("About")
                .setMessage("Music Player v1.0\nAn expert-crafted media experience.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
