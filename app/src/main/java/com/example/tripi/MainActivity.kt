package com.example.tripi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.tripi.databinding.ActivityMainBinding
import com.example.tripi.storage.StickerRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var profileIcon: ImageView
    private lateinit var walletIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StickerRepository.init(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Get NavController from NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Define bottom nav destinations
        val bottomNavDestinations = setOf(
            R.id.navigation_map,
            R.id.navigation_camera,
            R.id.navigation_quests,
            R.id.navigation_collection,
            R.id.navigation_wallet
        )

        val appBarConfiguration = AppBarConfiguration(bottomNavDestinations)
        navView.setupWithNavController(navController)

        // ✅ Inflate and attach the custom toolbar
        val toolbarContainer = binding.toolbarContainer
        val customToolbar = LayoutInflater.from(this).inflate(R.layout.custom_toolbar, toolbarContainer, false)
        toolbarContainer.addView(customToolbar)

        profileIcon = customToolbar.findViewById(R.id.profileIcon)
        walletIcon = customToolbar.findViewById(R.id.walletIcon)

        profileIcon.setOnClickListener {
            navController.navigate(R.id.navigation_profile)
        }

        walletIcon.setOnClickListener {
            navController.navigate(R.id.navigation_wallet)
        }

        // ✅ Show/hide toolbar based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in bottomNavDestinations) {
                toolbarContainer.visibility = View.VISIBLE
            } else {
                toolbarContainer.visibility = View.GONE
            }
        }
    }
}
