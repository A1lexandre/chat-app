package com.android.chatapp.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.chatapp.R
import com.android.chatapp.databinding.ActContactsBinding
import com.android.chatapp.databinding.ContactItemBinding
import com.android.chatapp.model.User
import com.android.chatapp.utils.Constants.app.USER
import com.android.chatapp.utils.Constants.app.USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.viewbinding.BindableItem

class ContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActContactsBinding
    private val adapter by lazy {
        GroupieAdapter().also { it.setOnItemClickListener(clickListener()) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvContacts.apply {
            adapter = this@ContactsActivity.adapter
            layoutManager = LinearLayoutManager(this@ContactsActivity)
        }
        fetchUsers()
    }

    private fun fetchUsers() {
        Firebase.firestore.collection(USERS).addSnapshotListener { value, error ->
            if(value != null) {
                val listOfContacts = value.toObjects(User::class.java)
                adapter.addAll(listOfContacts
                    .filter {
                        it.id != Firebase.auth.currentUser?.uid
                    }
                    .toUserItem())
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, error?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ContactItem(private val user: User): BindableItem<ContactItemBinding>() {
        override fun bind(binding: ContactItemBinding, p: Int) {
            binding.contactName.text = user.name
            Picasso.get().load(user.photoUrl).into(binding.contactPic)
        }

        override fun getLayout(): Int = R.layout.contact_item

        override fun initializeViewBinding(v: View): ContactItemBinding {
            return ContactItemBinding.bind(v)
        }

    }

    private fun clickListener(): OnItemClickListener = OnItemClickListener { item, _ ->
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(USER, item as User)
        startActivity(intent)
        finish()
    }

    private fun List<User>.toUserItem(): List<ContactItem> {
        return this.map {
            ContactItem(it)
        }
    }
}



