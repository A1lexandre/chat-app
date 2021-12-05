package com.android.chatapp

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(SessionTracker())
    }
}