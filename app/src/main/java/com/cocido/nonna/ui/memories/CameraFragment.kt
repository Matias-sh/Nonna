package com.cocido.nonna.ui.memories

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cocido.nonna.R
import com.cocido.nonna.databinding.FragmentCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para captura de fotos con CameraX
 */
@AndroidEntryPoint
class CameraFragment : Fragment() {
    
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CameraViewModel by viewModels()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startCamera(requireContext(), this, binding.previewView)
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara necesario", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        checkCameraPermission()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.buttonCapture.setOnClickListener {
            viewModel.takePhoto(requireContext())
        }
        
        binding.buttonSwitchCamera.setOnClickListener {
            viewModel.switchCamera(requireContext(), this, binding.previewView)
        }
        
        binding.buttonFlash.setOnClickListener {
            viewModel.toggleFlash()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CameraUiState.Loading -> {
                        showLoading(true)
                    }
                    is CameraUiState.PhotoCaptured -> {
                        showLoading(false)
                        // Navegar de vuelta con la foto capturada
                        val result = Bundle().apply {
                            putString("photo_uri", state.photoUri.toString())
                        }
                        parentFragmentManager.setFragmentResult("camera_result", result)
                        findNavController().navigateUp()
                    }
                    is CameraUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is CameraUiState.FlashToggled -> {
                        updateFlashButton(state.isFlashOn)
                    }
                    is CameraUiState.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.startCamera(requireContext(), this, binding.previewView)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonCapture.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun updateFlashButton(isFlashOn: Boolean) {
        binding.buttonFlash.setImageResource(
            if (isFlashOn) R.drawable.ic_launcher_foreground else R.drawable.ic_launcher_foreground
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopCamera()
        _binding = null
    }
}


