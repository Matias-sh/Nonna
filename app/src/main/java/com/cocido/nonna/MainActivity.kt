package com.cocido.nonna

import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cocido.nonna.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        setupWindowInsets()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Configurar BottomNavigationView
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Configurar FAB
        binding.fabAddMemory.setOnClickListener {
            navController.navigate(R.id.action_home_to_create_memory)
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
}