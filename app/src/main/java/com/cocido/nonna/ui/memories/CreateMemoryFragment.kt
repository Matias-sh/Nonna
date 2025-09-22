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
import com.cocido.nonna.databinding.FragmentCreateMemoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para crear nuevos recuerdos sensoriales
 * Wizard en 3 pasos: Foto → Audio → Metadatos
 */
@AndroidEntryPoint
class CreateMemoryFragment : Fragment() {
    
    private var _binding: FragmentCreateMemoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CreateMemoryViewModel by viewModels()
    
    private var selectedPhotoUri: Uri? = null
    private var recordedAudioPath: String? = null
    
    // Launchers para resultados de actividad
    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedPhotoUri?.let { uri ->
                viewModel.setPhotoUri(uri)
                binding.imageViewPhotoPreview.setImageURI(uri)
                binding.imageViewPhotoPreview.visibility = View.VISIBLE
            }
        }
    }
    
    private val choosePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedPhotoUri = it
            viewModel.setPhotoUri(it)
            binding.imageViewPhotoPreview.setImageURI(it)
            binding.imageViewPhotoPreview.visibility = View.VISIBLE
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        setupFragmentResultListener()
    }
    
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Botones de foto
        binding.buttonTakePhoto.setOnClickListener {
            // Navegar al CameraFragment
            findNavController().navigate(R.id.action_createMemory_to_camera)
        }
        
        binding.buttonChoosePhoto.setOnClickListener {
            choosePhotoLauncher.launch("image/*")
        }
        
        // Botones de audio
        binding.buttonRecord.setOnClickListener {
            checkMicrophonePermissionAndRecord()
        }
        
        binding.buttonStop.setOnClickListener {
            viewModel.stopRecording()
        }
        
        // Botón de guardar
        binding.buttonSave.setOnClickListener {
            saveMemory()
        }
        
        // Selector de fecha
        binding.editTextDate.setOnClickListener {
            // TODO: Implementar DatePicker
        }
    }
    
    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener("camera_result", this) { _, result ->
            val photoUriString = result.getString("photo_uri")
            photoUriString?.let { uriString ->
                val uri = Uri.parse(uriString)
                selectedPhotoUri = uri
                viewModel.setPhotoUri(uri)
                binding.imageViewPhotoPreview.setImageURI(uri)
                binding.imageViewPhotoPreview.visibility = View.VISIBLE
            }
        }
    }
    
    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePhoto()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun checkMicrophonePermissionAndRecord() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, continuar con la acción
        } else {
            Toast.makeText(requireContext(), "Permiso necesario para continuar", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun takePhoto() {
        // Crear archivo temporal para la foto
        val photoFile = viewModel.createPhotoFile(requireContext())
        selectedPhotoUri = photoFile
        takePhotoLauncher.launch(selectedPhotoUri)
    }
    
    private fun startRecording() {
        viewModel.startRecording(requireContext())
        binding.buttonRecord.visibility = View.GONE
        binding.buttonStop.visibility = View.VISIBLE
        binding.viewWaveform.visibility = View.VISIBLE
        binding.textViewDuration.visibility = View.VISIBLE
    }
    
    private fun saveMemory() {
        val title = binding.editTextTitle.text?.toString() ?: ""
        val date = binding.editTextDate.text?.toString() ?: ""
        val tags = binding.editTextTags.text?.toString() ?: ""
        
        if (title.isBlank()) {
            binding.textInputLayoutTitle.error = "El título es requerido"
            return
        }
        
        val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        viewModel.saveMemory(
            title = title,
            date = date,
            tags = tagList
        )
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CreateMemoryUiState.Loading -> {
                        showLoading(true)
                    }
                    is CreateMemoryUiState.Success -> {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Recuerdo guardado exitosamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is CreateMemoryUiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is CreateMemoryUiState.Recording -> {
                        updateRecordingUI(state)
                    }
                    is CreateMemoryUiState.Idle -> {
                        showLoading(false)
                    }
                }
            }
        }
    }
    
    private fun updateRecordingUI(state: CreateMemoryUiState.Recording) {
        binding.textViewDuration.text = state.duration
        // TODO: Actualizar waveform con los datos de amplitud
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonSave.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


