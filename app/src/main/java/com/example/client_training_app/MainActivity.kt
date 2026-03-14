package com.example.client_training_app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu()
        }

        // Connect Toolbar to NavController
        NavigationUI.setupActionBarWithNavController(this, navController)

        //  Back arrow click
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

    // 1. Zobrazíme ikonku v horní liště
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        // 1. Získáme NavController (abychom věděli, kde jsme)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. Zjistíme ID aktuální obrazovky
        val currentId = navController.currentDestination?.id

        // 3. Seznam ID, kde chceme domeček SKRÝT
        // (Zkontroluj si v nav_graph.xml, že se ta ID jmenují přesně takto!)
        val hideHomeButtonDestinations = setOf(
            R.id.homeFragment,              // Na domovské obrazovce je zbytečný
            R.id.activeWorkoutFragment,     // Aby si nesmazali trénink
            R.id.trainingUnitEditorFragment, // Aby nepřišli o úpravy tréninku
            R.id.addExerciseFragment,        // Aby nepřišli o rozdělaný cvik
            R.id.addProfileFragment         // Aby nepřišli o editování klienta
        )

        // 4. Pokud je aktuální obrazovka na "černé listině", skryjeme tlačítko
        if (currentId in hideHomeButtonDestinations) {
            menu?.findItem(R.id.action_home)?.isVisible = false
        } else {
            // Jinak ho pro jistotu zobrazíme (kdyby bylo skryté z minula)
            menu?.findItem(R.id.action_home)?.isVisible = true
        }

        return true
    }

    // 2. Co se stane, když na ni klikneš
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_home -> {
                // Najdeme NavController
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val navController = navHostFragment.navController

                navController.popBackStack(R.id.homeFragment, false)
                true
            }
            // Tady můžeš obsloužit i kliknutí na Profily, pokud ho přidáš do menu
            // R.id.action_profile -> { ... }

            else -> super.onOptionsItemSelected(item)
        }
    }
}