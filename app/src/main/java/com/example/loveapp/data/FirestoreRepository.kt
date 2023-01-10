package com.example.loveapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val USERS = "users"
const val REQUESTS = "requests"
const val NO_LOGIN_ERROR = "No current user logged in."

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun createUser() {
        val username = auth.currentUser?.displayName
            ?: throw Exception(NO_LOGIN_ERROR)
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        withContext(Dispatchers.IO) {
            val user = User(username, false, 0)
            db.collection(USERS).document(email).set(user).await()
        }
    }

    suspend fun isTaken(): Boolean {
        var isTaken = false
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        withContext(Dispatchers.IO) {
            db.collection(USERS).document(email).get()
                .addOnSuccessListener { document ->
                    isTaken = document.get("taken") as Boolean
                }
                .addOnFailureListener {
                    Log.i("FirestoreRepository", it.message.toString())
                }.await()
        }
        return isTaken
    }

    fun getAllRequests(): Flow<List<Request>> {
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        return db.collection(USERS)
            .document(email)
            .collection(REQUESTS)
            .snapshots().map { snapshot ->
                snapshot.toObjects(Request::class.java)
            }
    }


    suspend fun addLover(email: String, day: Int, month: Int, year: Int): Boolean? {
        var success: Boolean?
        val currentUserEmail = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        val currentUsername = auth.currentUser?.displayName
            ?: throw Exception(NO_LOGIN_ERROR)
        withContext(Dispatchers.IO) {

            val loverAccount = db.collection(USERS).document(email).get().await()
            Log.i("FirestoreRepository", "test value: $loverAccount")

            if (!loverAccount.data.isNullOrEmpty()) {
                db.collection(USERS).document(email)
                    .collection(REQUESTS).document(currentUserEmail)
                    .set(
                        hashMapOf(
                            "email" to currentUserEmail,
                            "name" to currentUsername,
                            "day" to day,
                            "month" to month,
                            "year" to year
                        )
                    ).await()
                success = true
            } else {
                Log.i("FirestoreRepository", "error")
                success = false
            }
        }
        return success
    }



    suspend fun deleteUser() {
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        return withContext(Dispatchers.IO) {
            db.collection(USERS).document(email).delete().await()
        }
    }
}

