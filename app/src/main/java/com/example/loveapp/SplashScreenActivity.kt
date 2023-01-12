package com.example.loveapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.loveapp.ui.account.login.AuthViewModel

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreenViewModel = ViewModelProvider(this)[SplashScreenViewModel::class.java]
        splashScreenViewModel.isTaken.observe(this){
            if (it) {
                Log.i("SplashScreenActivity", it.toString())
                startActivity(Intent(this, MainActivity::class.java))
            } else if(!it) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }
        setContentView(R.layout.activity_splash_screen)

    }
}