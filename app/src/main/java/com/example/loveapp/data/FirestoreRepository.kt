package com.example.loveapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getData(): User? {
        val uid = auth.currentUser?.uid
            ?: throw Exception("No current user logged in.")
        return withContext(Dispatchers.IO) {
            val document = db.collection("users").document(uid).get().await()
            document.toObject(User::class.java)
        }
    }

    suspend fun createUser() {
        val username = auth.currentUser?.displayName
            ?: throw Exception("No current user logged in.")
        val email = auth.currentUser?.email
            ?: throw Exception("No current user logged in.")
        withContext(Dispatchers.IO) {
            val user = User(username, false, 0)
            db.collection("users").document(email).set(user).await()
        }
    }

    suspend fun isTaken(): Boolean {
        var isTaken = false
        val email = auth.currentUser?.email
            ?: throw Exception("No current user logged in.")
        withContext(Dispatchers.IO) {
            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    isTaken = document.get("taken") as Boolean
                }
                .addOnFailureListener {
                    Log.i("FirestoreRepository", it.message.toString())
                }.await()
        }
        return isTaken
    }

    suspend fun addLover(email: String){
        val currentUserEmail = auth.currentUser?.email
            ?: throw Exception("No current user logged in.")
        withContext(Dispatchers.IO) {
            db.collection("users").document(currentUserEmail)
                .collection("requests").document(email)
                .set(hashMapOf(
                    "email" to currentUserEmail
                )).await()
        }
    }

    suspend fun deleteUser() {
        val email = auth.currentUser?.email
            ?: throw Exception("No current user logged in.")
        return withContext(Dispatchers.IO) {
            db.collection("users").document(email).delete().await()
        }
    }
}

