package com.musicplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.musicplayer.databinding.FragmentEqualizerBinding
import com.musicplayer.ui.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.media3.common.util.UnstableApi

import com.musicplayer.models.DSPPresets
import com.google.android.material.chip.Chip

@UnstableApi
class EqualizerFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEqualizerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEqualizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupPresets()
    }

    private fun setupPresets() {
        val context = requireContext()
        DSPPresets.ALL.forEach { preset ->
            val chip = Chip(context).apply {
                text = preset.name
                isCheckable = true
                setOnClickListener {
                    applyPreset(preset.gains)
                }
            }
            binding.presetGroup.addView(chip)
        }
    }

    private fun applyPreset(gains: FloatArray) {
        val bands = listOf(binding.band1, binding.band2, binding.band3, binding.band4, binding.band5, binding.band6)
        gains.forEachIndexed { index, gain ->
            bands[index].value = gain
            viewModel.setEqGain(index, gain)
        }
    }

    private fun setupUI() {
        val settings = viewModel.getAudioSettings()

        // Band Sliders
        val bands = listOf(binding.band1, binding.band2, binding.band3, binding.band4, binding.band5, binding.band6)
        bands.forEachIndexed { index, slider ->
            slider.value = settings.bandGains[index]
            slider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    viewModel.setEqGain(index, value)
                }
            }
        }

        // Toggles
        binding.switchAdaptive.isChecked = settings.adaptiveEnabled
        binding.switchAdaptive.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAdaptiveGenreEnabled(isChecked)
        }
        
        binding.switchLUFS.isChecked = settings.lufsEnabled
        binding.switchLUFS.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setLUFSEnabled(isChecked)
        }
        
        binding.switchPsycho.isChecked = settings.psychoEnabled
        binding.switchPsycho.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPsychoacousticEnabled(isChecked)
        }
        
        binding.switchStereo.isChecked = settings.stereoEnabled
        binding.switchStereo.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setStereoWideningEnabled(isChecked)
        }

        binding.sliderTilt.value = settings.tiltValue
        binding.sliderTilt.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setSpectralTilt(value)
            }
        }

        binding.btnReset.setOnClickListener {
            bands.forEachIndexed { index, it -> 
                it.value = 0f 
                viewModel.setEqGain(index, 0f)
            }
            binding.switchAdaptive.isChecked = true
            binding.switchLUFS.isChecked = true
            binding.switchPsycho.isChecked = true
            binding.switchStereo.isChecked = true
            binding.sliderTilt.value = 0f
            viewModel.setAdaptiveGenreEnabled(true)
            viewModel.setLUFSEnabled(true)
            viewModel.setPsychoacousticEnabled(true)
            viewModel.setStereoWideningEnabled(true)
            viewModel.setSpectralTilt(0f)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EqualizerFragment()
    }
}
