package com.example.loveapp.data

import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    fun logout(){
        auth.signOut()
    }


    suspend fun createUser() {
        val username = auth.currentUser?.displayName
            ?: throw Exception(NO_LOGIN_ERROR)
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        withContext(Dispatchers.IO) {
            val user = User(email, username, false, 0)
            val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
            db.collection(USERS).document(encodedMail).set(user).await()
        }
    }

    fun onStart(): Boolean {
        var isTaken = false
        val email = auth.currentUser?.email
            ?: return false
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        db.collection(USERS).document(encodedMail).get()
            .addOnSuccessListener { document ->
                isTaken = document.get("taken") as Boolean
                Log.i("FirestoreRepository", "insucceslistener" + isTaken.toString())
            }
            .addOnFailureListener {
                Log.i("FirestoreRepository", it.message.toString())
            }
        Log.i("FirestoreRepository", isTaken.toString())
        return isTaken
    }

    suspend fun checkIsTakenOnce(): Boolean {
        var isTaken = false
        val email = auth.currentUser?.email
            ?: return isTaken
        withContext(Dispatchers.IO) {
            val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
            db.collection(USERS).document(encodedMail).get()
                .addOnSuccessListener { document ->
                    isTaken = document.get("taken") as Boolean
                }
                .addOnFailureListener {
                    Log.i("FirestoreRepository", it.message.toString())
                }.await()
        }
        return isTaken
    }

    fun isTaken(): Flow<Boolean> {
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        return db.collection(USERS)
            .document(encodedMail)
            .snapshots().map { snapshot ->
                snapshot.get("taken") as Boolean
            }
    }

    fun getAllRequests(): Flow<List<Request>> {
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        return db.collection(USERS)
            .document(encodedMail)
            .collection(REQUESTS)
            .snapshots().map { snapshot ->
                snapshot.toObjects(Request::class.java)
            }
    }


    suspend fun addLover(email: String, date: List<Int>): String? {
        var returnValue: String?
        val currentUserEmail = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        val currentUsername = auth.currentUser?.displayName
            ?: throw Exception(NO_LOGIN_ERROR)
        val encodedMail = Base64.encodeToString(currentUserEmail.toByteArray(), Base64.DEFAULT)
        val encodedLoverMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)

        if (email == currentUserEmail) {
            returnValue = "You cannot add yourself."
            return returnValue
        }

        withContext(Dispatchers.IO) {

            val loverAccount = db.collection(USERS).document(encodedLoverMail).get().await()
            Log.i("FirestoreRepository", "test value: $loverAccount")
            val isTaken = loverAccount.get("taken") as Boolean

            if (!loverAccount.data.isNullOrEmpty() && !isTaken) {
                db.collection(USERS).document(encodedLoverMail)
                    .collection(REQUESTS).document(encodedMail)
                    .set(
                        hashMapOf(
                            "email" to currentUserEmail,
                            "name" to currentUsername,
                            "date" to date
                        )
                    ).await()
                returnValue = null
            } else if(isTaken){
                Log.i("FirestoreRepository", "error")
                returnValue = "User is already taken."
            } else {
                returnValue = "User does not exist."
            }
        }
        return returnValue
    }

    suspend fun declineRequest(request: Request) {
        val currentUserEmail = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        val encodedMail = Base64.encodeToString(currentUserEmail.toByteArray(), Base64.DEFAULT)
        val encodedLoverMail = Base64.encodeToString(request.email.toByteArray(), Base64.DEFAULT)
        Log.i("FirestoreRepository", "$request.")
        withContext(Dispatchers.IO) {
            db.collection(USERS).document(encodedMail)
                .collection(REQUESTS).document(encodedLoverMail)
                .delete()
                .await()
        }
    }

    suspend fun acceptRequest(request: Request) {
        val currentUserEmail = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        val currentUsername = auth.currentUser?.displayName
            ?: throw Exception(NO_LOGIN_ERROR)

        val encodedMail = Base64.encodeToString(currentUserEmail.toByteArray(), Base64.DEFAULT)
        val encodedLoverMail = Base64.encodeToString(request.email.toByteArray(), Base64.DEFAULT)

        withContext(Dispatchers.IO) {
            val coupleRef = db.collection("couples")
                .add(
                    hashMapOf(
                        "firstPerson" to currentUsername,
                        "secondPerson" to request.name,
                        "date" to request.date
                    )
                ).await()
            val coupleId = coupleRef.id

            db.collection(USERS).document(encodedMail).update("taken", true, "coupleId", coupleId)
                .await()
            db.collection(USERS).document(encodedLoverMail)
                .update("taken", true, "coupleId", coupleId).await()
            val querySnapshot =
                db.collection(USERS).document(encodedMail).collection(REQUESTS).get().await()
            val querySnapshot2 =
                db.collection(USERS).document(encodedLoverMail).collection(REQUESTS).get().await()
            val batch = db.batch()
            for (document in querySnapshot) {
                batch.delete(document.reference)
            }
            for (document in querySnapshot2) {
                batch.delete(document.reference)
            }
            batch.commit().await()

        }


    }


    suspend fun deleteUser() {
        val email = auth.currentUser?.email
            ?: throw Exception(NO_LOGIN_ERROR)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        return withContext(Dispatchers.IO) {
            db.collection(USERS).document(encodedMail).delete().await()
        }
    }
}

