package com.gozem.test.businesscase

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.gozem.test.businesscase.utils.AppState
import com.gozem.test.businesscase.viewModels.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initListener()
        initObservable()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun initView() {
        viewModel.start(this)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }

    private fun initListener() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (navController.currentDestination != null) {
                        if (navController.currentDestination?.id
                            == R.id.splashScreenFragment) {
                            finish()
                        } else {
                            navController.navigateUp()
                        }
                    }
                }
            }

        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun initObservable() {
        viewModel.appState.observe(this, {
            when(it!!) {
                AppState.SIGN_IN_SUCCESS -> {
                    startActivity(Intent(this@MainActivity,
                        HomeActivity::class.java))
                    this.finish()
                }
                AppState.SIGN_IN_FAILURE -> { }
            }
        })
    }
}