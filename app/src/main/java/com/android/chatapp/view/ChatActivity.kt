package com.android.chatapp.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.chatapp.R
import com.android.chatapp.databinding.ActChatBinding
import com.android.chatapp.model.Message
import com.android.chatapp.model.User
import com.android.chatapp.utils.Constants.app.CONVERSATIONS
import com.android.chatapp.utils.Constants.app.TIMESTAMP
import com.android.chatapp.utils.Constants.app.USER
import com.android.chatapp.utils.Constants.app.USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.admin.v1.Index
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActChatBinding
    private lateinit var contact: User
    private lateinit var me: User
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
        binding = ActChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getConversationParticipants()

        supportActionBar?.title = contact.name
        binding.rvMessages.apply {
            adapter = this@ChatActivity.adapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }

        binding.btnSendMessage.setOnClickListener { sendMessage() }
    }

    private fun sendMessage() {
        val txtField = binding.editMessage
        val msg = txtField.text.toString().trim()
        txtField.text = null
        if(msg.isNotEmpty()) {
            val message = Message(
                msg = msg,
                timestamp = System.currentTimeMillis(),
                fromId = me.id,
                toId = contact.id
            )
            val conversationsRef = firestore.collection(CONVERSATIONS)
            conversationsRef.document(message.fromId)
                .collection(message.toId)
                .add(message).addOnCompleteListener {
                    if(it.isSuccessful)
                        conversationsRef.document(message.toId)
                            .collection(message.fromId)
                            .add(message)
                    else
                        Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun fetchMessages() {
        firestore.collection(CONVERSATIONS).document(me.id)
            .collection(contact.id)
            .orderBy(TIMESTAMP, Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->
            if(value != null) {
                val changes = value.documentChanges
                for(change in changes){
                    if(change.type.equals(DocumentChange.Type.ADDED)) {
                        val message = change.document.toObject(Message::class.java)
                        adapter.add(MessageItem(message))
                        binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }
    }

    private fun getConversationParticipants() {
        contact = intent.getParcelableExtra(USER) ?: User()
        firestore.collection(USERS).document(auth.currentUser?.uid ?: "").get().addOnSuccessListener {
            it.toObject(User::class.java)?.let { user ->
                me = user
                fetchMessages()
            } }
    }

    inner class MessageItem(private val message: Message): Item<GroupieViewHolder>() {

        override fun bind(viewholder: GroupieViewHolder, position: Int) {
            val photoContainer = viewholder.root.findViewById<CircleImageView>(R.id.img_contact_photo)
            val msgContainer = viewholder.root.findViewById<TextView>(R.id.txt_msg)
            val photoUrl = if(message.fromId.equals(me.id)) me.photoUrl else contact.photoUrl
            Picasso.get().load(photoUrl).into(photoContainer)
            msgContainer.text = message.msg
        }

        override fun getLayout(): Int {
            return if(message.fromId.equals(me.id)) R.layout.to_message_item else R.layout.from_message_item
        }

    }

}