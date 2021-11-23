package com.android.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.chatapp.databinding.ActLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickables()
    }

    private fun setupClickables() {
        binding.apply {
            txtSignup.setOnClickListener{
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }
}