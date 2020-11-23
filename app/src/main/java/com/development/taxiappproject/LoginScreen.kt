package com.development.taxiappproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.development.taxiappproject.databinding.ActivityLoginScreenBinding

class LoginScreen : AppCompatActivity() {
  lateinit var loginScreen: ActivityLoginScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginScreen =  DataBindingUtil.setContentView(this, R.layout.activity_login_screen)
        loginScreen.forgotPassword.setOnClickListener{
            val mainActivityIntent = Intent(applicationContext, ForgotPassword::class.java)
            startActivity(mainActivityIntent)
        }

        loginScreen.signup.setOnClickListener{
            val mainActivityIntent = Intent(applicationContext, SignUpScreen::class.java)
            startActivity(mainActivityIntent)
        }

        loginScreen.loginButton.setOnClickListener{
            val mainActivityIntent = Intent(applicationContext, HomeScreen::class.java)
            startActivity(mainActivityIntent)
        }
    }

}
