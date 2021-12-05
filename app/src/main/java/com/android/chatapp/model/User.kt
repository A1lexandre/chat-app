package com.android.chatapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String = "",
    val photoUrl: String = "",
    val id: String = "",
    val online: Boolean = true
) : Parcelable
