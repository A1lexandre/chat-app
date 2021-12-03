package com.android.chatapp.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.chatapp.databinding.ActChatBinding
import com.android.chatapp.model.User
import com.android.chatapp.utils.Constants.app.USER
import com.xwray.groupie.GroupieAdapter

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActChatBinding
    private lateinit var contact: User
    private val adapter by lazy {
        GroupieAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contact = intent.getParcelableExtra(USER) ?: User()
        binding.rvMessages.apply {
            adapter = this@ChatActivity.adapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }
}