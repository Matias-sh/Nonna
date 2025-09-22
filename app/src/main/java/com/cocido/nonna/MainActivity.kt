package com.cocido.nonna

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cocido.nonna.databinding.ActivityMainBinding
import com.cocido.nonna.domain.usecase.CheckAuthStatusUseCase
import com.cocido.nonna.data.manager.AuthStateManager
import com.cocido.nonna.data.manager.AuthStateEvent
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var checkAuthStatusUseCase: CheckAuthStatusUseCase

    @Inject
    lateinit var authStateManager: AuthStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAuthStatus()
        setupNavigation()
        setupWindowInsets()
        observeAuthState()
    }
    
    private fun checkAuthStatus() {
        lifecycleScope.launch {
            val isAuthValid = checkAuthStatusUseCase()
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            if (isAuthValid) {
                // Si hay sesión válida, navegar al home
                if (navController.currentDestination?.id == R.id.loginFragment) {
                    navController.navigate(R.id.action_login_to_home)
                }
            } else {
                // Si no hay sesión válida, asegurar que estamos en login
                if (navController.currentDestination?.id != R.id.loginFragment) {
                    navController.navigate(R.id.loginFragment)
                }
            }
        }
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Configurar BottomNavigationView
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Configurar FAB
        binding.fabAddMemory.setOnClickListener {
            // Navegar a create memory desde cualquier pantalla
            try {
                navController.navigate(R.id.action_home_to_create_memory)
            } catch (e: IllegalArgumentException) {
                // Si no existe la acción desde el fragmento actual, navegar directamente
                navController.navigate(R.id.createMemoryFragment)
            }
        }
        
        // Ocultar elementos en pantallas de auth
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNavigation.visibility = android.view.View.GONE
                    binding.fabAddMemory.visibility = android.view.View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = android.view.View.VISIBLE
                    binding.fabAddMemory.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            authStateManager.authStateEvents.collect { event ->
                when (event) {
                    is AuthStateEvent.TokenExpired,
                    is AuthStateEvent.LoggedOut -> {
                        // Token expiró o usuario hizo logout, navegar al login
                        val navHostFragment = supportFragmentManager
                            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                        val navController = navHostFragment.navController

                        if (navController.currentDestination?.id != R.id.loginFragment) {
                            navController.navigate(R.id.loginFragment)
                        }
                    }
                    is AuthStateEvent.LoggedIn -> {
                        // Usuario se logueó exitosamente
                        // El navigation ya se maneja en LoginFragment
                    }
                }
            }
        }
    }
}