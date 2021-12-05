package com.android.chatapp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.android.chatapp.utils.Constants.app.ONLINE
import com.android.chatapp.utils.Constants.app.USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SessionTracker: LifecycleEventObserver {

    private val auth by lazy {
        Firebase.auth
    }

    private val firestore by lazy {
        Firebase.firestore
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                auth.currentUser?.let {
                    firestore.collection(USERS).document(it.uid)
                        .update(ONLINE, true)
                }
            }
            Lifecycle.Event.ON_STOP -> {
                auth.currentUser?.let {
                    firestore.collection(USERS).document(it.uid)
                        .update(ONLINE, false)
                }
            }
        }

    }
}