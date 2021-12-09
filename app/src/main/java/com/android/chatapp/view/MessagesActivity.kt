package com.android.chatapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.chatapp.R
import com.android.chatapp.databinding.ActMessagesBinding
import com.android.chatapp.databinding.LastMessageItemBinding
import com.android.chatapp.model.LastMessage
import com.android.chatapp.model.User
import com.android.chatapp.utils.Constants.app.CONTACTS
import com.android.chatapp.utils.Constants.app.LAST_MESSAGES
import com.android.chatapp.utils.Constants.app.ONLINE
import com.android.chatapp.utils.Constants.app.TIMESTAMP
import com.android.chatapp.utils.Constants.app.USER
import com.android.chatapp.utils.Constants.app.USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.viewbinding.BindableItem

class MessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActMessagesBinding
    private val adapter by lazy {
        GroupieAdapter()
    }

    private val auth by lazy {
        Firebase.auth
    }
    private val firestore by lazy {
        Firebase.firestore
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.messages_screen_name)
        verifyAuthentication()
        fetchLastMessages()
    }

    private fun verifyAuthentication() {
        if(auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun fetchLastMessages() {
        binding.rvLastMessages.apply {
            adapter = this@MessagesActivity.adapter.apply {
                setOnItemClickListener(itemClickListener())
            }
            layoutManager = LinearLayoutManager(this@MessagesActivity)
        }
        firestore.collection(LAST_MESSAGES).document(auth.uid ?: "")
            .collection(CONTACTS)
            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->
                if(value?.documentChanges != null)
                    for(change in value.documentChanges)
                        if (change.type.equals(DocumentChange.Type.ADDED))
                            adapter.add(
                                ConversationItem(change.document.toObject(LastMessage::class.java))
                            )
            }
    }

    private fun itemClickListener() = OnItemClickListener { item, _ ->
        firestore.collection(USERS).document((item as ConversationItem).getLastMessage().userId)
            .get().addOnSuccessListener {
                val intent = Intent(this@MessagesActivity, ChatActivity::class.java)
                intent.putExtra(USER, it.toObject(User::class.java))
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
                firestore.collection(USERS).document(auth.currentUser?.uid ?: "")
                    .update(ONLINE, false)
                auth.signOut()
                verifyAuthentication()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private class ConversationItem(private val lastMessage: LastMessage): BindableItem<LastMessageItemBinding>() {
        override fun bind(binding: LastMessageItemBinding, pos: Int) {
            binding.apply {
                lastMessage.apply {
                    contactName.text = userName
                    txtMsg.text = textMessage
                    Picasso.get().load(photoUrl).into(imgContactPhoto)
                }
            }
        }

        override fun getLayout(): Int {
            return R.layout.last_message_item
        }

        override fun initializeViewBinding(view: View): LastMessageItemBinding {
            return LastMessageItemBinding.bind(view)
        }

        fun getLastMessage(): LastMessage {
            return this.lastMessage
        }

    }
}