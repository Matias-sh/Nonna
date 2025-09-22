package com.cocido.nonna.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cocido.nonna.R
import com.cocido.nonna.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { uri ->
                viewModel.updateProfileImage(uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        viewModel.loadUserProfile()
    }

    private fun setupUI() {
        binding.apply {
            btnEditProfile.setOnClickListener {
                enableEditMode()
            }
            
            btnSaveProfile.setOnClickListener {
                saveProfile()
            }
            
            btnCancelEdit.setOnClickListener {
                disableEditMode()
                viewModel.loadUserProfile()
            }
            
            btnChangePhoto.setOnClickListener {
                requestImagePermission()
            }
            
            btnLogout.setOnClickListener {
                viewModel.logout()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ProfileUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.contentLayout.visibility = View.GONE
                    }
                    is ProfileUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        displayUserProfile(state.user)
                    }
                    is ProfileUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    is ProfileUiState.LoggedOut -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                        // Navegar al login
                        findNavController().navigate(R.id.action_profile_to_login)
                    }
                    is ProfileUiState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun displayUserProfile(user: com.cocido.nonna.data.remote.dto.UserDto) {
        binding.apply {
            tvUserName.text = user.name
            tvUserEmail.text = user.email
            
            // Cargar imagen de perfil
            if (!user.avatar.isNullOrEmpty()) {
                // TODO: Implementar carga de imagen con Glide o Picasso
                ivProfilePhoto.setImageResource(R.drawable.ic_launcher_foreground)
            } else {
                ivProfilePhoto.setImageResource(R.drawable.ic_launcher_foreground)
            }
            
            // Llenar campos de edición
            etEditName.setText(user.name)
            etEditEmail.setText(user.email)
        }
    }

    private fun enableEditMode() {
        binding.apply {
            btnEditProfile.visibility = View.GONE
            btnSaveProfile.visibility = View.VISIBLE
            btnCancelEdit.visibility = View.VISIBLE
            btnChangePhoto.visibility = View.VISIBLE
            
            etEditName.visibility = View.VISIBLE
            etEditEmail.visibility = View.VISIBLE
            tvUserName.visibility = View.GONE
            tvUserEmail.visibility = View.GONE
        }
    }

    private fun disableEditMode() {
        binding.apply {
            btnEditProfile.visibility = View.VISIBLE
            btnSaveProfile.visibility = View.GONE
            btnCancelEdit.visibility = View.GONE
            btnChangePhoto.visibility = View.GONE
            
            etEditName.visibility = View.GONE
            etEditEmail.visibility = View.GONE
            tvUserName.visibility = View.VISIBLE
            tvUserEmail.visibility = View.VISIBLE
        }
    }

    private fun saveProfile() {
        val name = binding.etEditName.text.toString().trim()
        val email = binding.etEditEmail.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.updateProfile(name, email)
    }

    private fun requestImagePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_IMAGE_PERMISSION
            )
        } else {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_IMAGE_PERMISSION = 1001
    }
}
