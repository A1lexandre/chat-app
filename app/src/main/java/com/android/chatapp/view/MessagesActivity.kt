package com.android.chatapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.android.chatapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessagesActivity : AppCompatActivity() {
    private val auth by lazy {
        Firebase.auth
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_messages)
        supportActionBar?.title = getString(R.string.messages_screen_name)
        verifyAuthentication()
    }

    private fun verifyAuthentication() {
        if(auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.contacts_item -> {
                startActivity(Intent(this, ContactsActivity::class.java))
            }
            R.id.logout_item -> {
                auth.signOut()
                verifyAuthentication()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}