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
        // This will show the back arrow when you navigate to other fragments
        NavigationUI.setupActionBarWithNavController(this, navController)

        // Handle hamburger menu icon click (when on home screen)
        binding.topAppBar.setNavigationOnClickListener {
            // TODO: Open navigation drawer or handle menu click
            // For now, let's just show which fragment we're on
            when (navController.currentDestination?.id) {
                R.id.homeFragment -> {
                    // On home - hamburger menu clicked
                    // TODO: Open drawer or show menu
                }
                else -> {
                    // On other screens - back button clicked
                    navController.navigateUp()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }

    //Add menu items to the toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

}