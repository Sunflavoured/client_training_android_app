package com.example.client_training_app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.client_training_app.databinding.ActivityMainBinding

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Třídu pro JSON přesuneme sem nahoru (nebo úplně mimo MainActivity)
    data class GitHubRelease(val tag_name: String, val html_url: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Toolbar as ActionBar
        setSupportActionBar(binding.topAppBar)

        // Get NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, _, _ ->
            invalidateOptionsMenu()
        }

        // Connect Toolbar to NavController
        NavigationUI.setupActionBarWithNavController(this, navController)

        // Back arrow click
        binding.topAppBar.setNavigationOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.homeFragment -> {
                    // prostor pro Hamburger menu (otevření šuplíku)
                }
                else -> {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        // ZDE JEN ZAVOLÁME FUNKCI PRO KONTROLU AKTUALIZACÍ
        checkForUpdates(this)
    }

    // --- FUNKCE JSOU NYNÍ VENKU Z onCreate ---

    private fun checkForUpdates(activity: android.app.Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TADY ZKONTROLUJ TU URL (musí začínat api.github.com/repos/...)//releases
                val urlString = "https://api.github.com/repos/Sunflavoured/client_training_android_app/releases/latest"
                Log.d("UpdateCheck", "Zkouším se připojit na: $urlString")

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

                val responseCode = connection.responseCode
                Log.d("UpdateCheck", "GitHub odpověděl kódem: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("UpdateCheck", "Odpověď z GitHubu: $response") // Vypíše, co přesně přišlo

                    val release = Gson().fromJson(response, GitHubRelease::class.java)

                    val latestVersion = release.tag_name.replace("v", "")
                    val currentVersion = BuildConfig.VERSION_NAME

                    Log.d("UpdateCheck", "Moje verze: $currentVersion, GitHub verze: $latestVersion")

                    if (latestVersion != currentVersion) {
                        Log.d("UpdateCheck", "Verze se liší, zobrazuji dialog!")
                        withContext(Dispatchers.Main) {
                            showUpdateDialog(activity, release.html_url)
                        }
                    } else {
                        Log.d("UpdateCheck", "Máme nejnovější verzi, nic nedělám.")
                    }
                } else {
                    Log.e("UpdateCheck", "Chyba API. Kód: $responseCode")
                }
            } catch (e: Exception) {
                // TADY SE ZACHYTÍ TA TVOJE ILLEGAL CHYBA
                Log.e("UpdateCheck", "Spadlo to na kritické chybě!", e)
            }
        }
    }

    private fun showUpdateDialog(activity: android.app.Activity, releaseUrl: String) {
        AlertDialog.Builder(activity)
            .setTitle("Nová verze k dispozici! \uD83D\uDE80") // Raketa emoji
            .setMessage("Je dostupná nová aktualizace aplikace. Doporučujeme si ji stáhnout pro ty nejlepší funkce a opravy. Tvoje tréninky zůstanou zachovány.")
            .setPositiveButton("Stáhnout aktualizaci") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                activity.startActivity(intent)
            }
            .setNegativeButton("Později", null)
            .show()
    }

    // --- ZBYTEK TVÉHO KÓDU PRO MENU ---

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val currentId = navController.currentDestination?.id

        val hideHomeButtonDestinations = setOf(
            R.id.homeFragment,
            R.id.activeWorkoutFragment,
            R.id.trainingUnitEditorFragment,
            R.id.addExerciseFragment,
            R.id.addProfileFragment
        )

        if (currentId in hideHomeButtonDestinations) {
            menu?.findItem(R.id.action_home)?.isVisible = false
        } else {
            menu?.findItem(R.id.action_home)?.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_home -> {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.popBackStack(R.id.homeFragment, false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}