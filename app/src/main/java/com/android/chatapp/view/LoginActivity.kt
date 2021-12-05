package com.android.chatapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.chatapp.databinding.ActLoginBinding
import com.android.chatapp.utils.Constants.app.ONLINE
import com.android.chatapp.utils.Constants.app.USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActLoginBinding
    private val auth by lazy {
        Firebase.auth
    }

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

            btnEnter.setOnClickListener {
                login()
            }
        }
    }

    private fun login() = with(binding) {
        val email = binding.editEmail.text?.trim().toString()
        val pass = binding.editPassword.text?.trim().toString()

        if(email.isNotEmpty() && pass.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        Firebase.firestore.collection(USERS).document(auth.uid ?: "").update(ONLINE, true)
                        val intent = Intent(this@LoginActivity, MessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(this@LoginActivity, it.exception?.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

}