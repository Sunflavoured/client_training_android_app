package com.example.client_training_app

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.client_training_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Toolbar as ActionBar
        setSupportActionBar(binding.topAppBar)

        // Get NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connect Toolbar to NavController
        NavigationUI.setupActionBarWithNavController(this, navController)

        // Handle hamburger menu / Back arrow click
        binding.topAppBar.setNavigationOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.homeFragment -> {
                    // prostor pro Hamburger menu (otevření šuplíku)
                }
                else -> {
                    // Zavoláme systémové zpět.
                    // To aktivuje OnBackPressedCallback ve tvém EditorFragmentu.
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // I zde je lepší volat onBackPressed, aby byla logika jednotná,
        // kdyby se náhodou listener nespustil (ale ten výše má přednost).
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
}