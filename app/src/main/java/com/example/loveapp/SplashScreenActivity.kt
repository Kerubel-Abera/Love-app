package com.example.loveapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreenViewModel = ViewModelProvider(this)[SplashScreenViewModel::class.java]
        Log.i("test", "splashscreenactivity called")
        splashScreenViewModel.isTaken.observe(this) { isTaken ->
            if (isTaken == true) {
                startActivity(Intent(this, MainActivity::class.java))
            } else if (isTaken == false) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }


    }
}