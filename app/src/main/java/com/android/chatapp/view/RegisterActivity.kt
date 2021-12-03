package com.android.chatapp.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import com.android.chatapp.model.User
import com.android.chatapp.databinding.ActRegisterBinding
import com.android.chatapp.utils.Constants.app.USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActRegisterBinding
    private lateinit var getPhoto: ActivityResultLauncher<String>
    private lateinit var selectedPhoto: Uri
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val firestore: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPhoto = registerForActivityResult(GetContent(), setPhoto())

        setupClickables()
    }

    private fun setupClickables() = with(binding) {
        btnSetPhoto.setOnClickListener {
            getPhoto.launch("image/*")
        }

        btnRegister.setOnClickListener {
            checkForm()
        }
    }


    private fun checkForm() {
        binding.apply {
            if(!(editEmail.text?.trim().isNullOrEmpty() ||
                    editName.text?.trim().isNullOrEmpty() ||
                        editPassword.text?.trim().isNullOrEmpty()))
                            createUser()
        }
    }

    private fun createUser() {
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                saveUserToDB()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToDB() {
        val filename = UUID.randomUUID()
        val ref = Firebase.storage.getReference("/images/$filename")
        ref.putFile(selectedPhoto)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val id = auth.currentUser?.uid ?: ""
                    val user = User(
                            name = binding.editName.text.toString(),
                            photoUrl = uri.toString(),
                            id = id
                    )
                    firestore.collection(USERS).document(id)
                        .set(user).addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                val intent = Intent(this, MessagesActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            } else {
                                auth.currentUser?.delete()
                                ref.delete()
                                Toast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .addOnFailureListener {
                auth.currentUser?.delete()
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setPhoto() : ActivityResultCallback<Uri> {
            return ActivityResultCallback<Uri> { result ->
                selectedPhoto = result
                binding.apply {
                    imgSelectedPhoto.setImageURI(selectedPhoto)
                    btnSetPhoto.alpha = 0f
                }
            }
    }
}