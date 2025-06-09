package com.example.tripi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tripi.databinding.ActivityMainBinding
import com.example.tripi.storage.StickerRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var profileIcon: ImageView
    private lateinit var walletIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init repository
        StickerRepository.init(applicationContext)

        // Inflate view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val toolbarContainer = binding.toolbarContainer
        val secondaryToolbar: MaterialToolbar = binding.secondaryToolbar

        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(navController.graph.startDestinationId, false)
            .build()


        // Define bottom nav destinations
        val bottomNavDestinations = setOf(
            R.id.navigation_quests,
            R.id.navigation_map,
            R.id.navigation_camera,
            R.id.navigation_collection,
            R.id.navigation_network
        )

        val appBarConfiguration = AppBarConfiguration(bottomNavDestinations)

        // Setup ActionBar for inner screens (uses secondary toolbar)
        setSupportActionBar(secondaryToolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        secondaryToolbar.setupWithNavController(navController, appBarConfiguration)

        // Setup bottom nav
        navView.setupWithNavController(navController)

        // Inflate and set up custom toolbar (for bottom nav screens)
        val customToolbar = LayoutInflater.from(this)
            .inflate(R.layout.custom_toolbar, toolbarContainer, false)
        toolbarContainer.addView(customToolbar)

        profileIcon = customToolbar.findViewById(R.id.profileIcon)
        walletIcon = customToolbar.findViewById(R.id.walletIcon)

        profileIcon.setOnClickListener {
            navController.navigate(R.id.navigation_profile, null, navOptions)
        }

        walletIcon.setOnClickListener {
            navController.navigate(R.id.navigation_wallet, null, navOptions)
        }


        // Switch toolbar based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in bottomNavDestinations) {
                toolbarContainer.visibility = View.VISIBLE
                secondaryToolbar.visibility = View.GONE
            } else {
                toolbarContainer.visibility = View.GONE
                secondaryToolbar.visibility = View.VISIBLE
            }
        }
    }
}
