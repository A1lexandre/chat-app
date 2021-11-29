package com.android.chatapp.view

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import com.android.chatapp.R
import com.android.chatapp.model.User
import com.android.chatapp.databinding.ActRegisterBinding
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
                ref.downloadUrl.addOnSuccessListener {
                    val user = User(
                            name = binding.editName.text.toString(),
                            photoUrl = it.toString(),
                            id = auth.currentUser?.uid ?: ""
                    )
                    firestore.collection("users").document()
                        .set(user).addOnCompleteListener {
                            if(it.isSuccessful) {
                                TODO()
                            } else {
                                auth.currentUser?.delete()
                                ref.delete()
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