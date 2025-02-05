package com.shaadow.tunes.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun submitFeedback(
        publicKey: String,
        privateKey: String,
        username: String,
        message: String,
        deviceModel: String,
        callback: (Boolean) -> Unit
    ) {
        val feedback = hashMapOf(
            "publicKey" to publicKey,
            "privateKey" to privateKey,
            "username" to username,
            "message" to message,
            "deviceModel" to deviceModel,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("feedbacks").add(feedback)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
