package com.example.loveapp.data

import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val USERS = "users"
const val COUPLES = "couples"
const val REQUESTS = "requests"
const val NO_LOGIN_ERROR = "No current user logged in."
const val NAME = 1
const val MAIL = 2
const val ID = 3

class FirestoreRepository private constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        private var instance: FirestoreRepository? = null
        fun getInstance(): FirestoreRepository {
            if (instance == null) {
                instance = FirestoreRepository()
            }
            return instance!!
        }
    }

    /** Simple function that returns the firebase user. **/
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    /** Simple function that logs the firebase user out. **/
    fun logout() {
        auth.signOut()
    }

    suspend fun getUserIcons(): List<Uri?> {

        var coupleId = ""
        val email = getMailOrName(MAIL)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)

        withContext(Dispatchers.IO) {
            val user = db.collection(USERS).document(encodedMail).get().await()
            coupleId = user.get("coupleId") as String
        }

        val links = listOf(
            "couples/$coupleId/firstPersonIcon.jpg",
            "couples/$coupleId/secondPersonIcon.jpg"
        )

        val firstImageRef = storage.reference.child(links[0])
        val secondImageRef = storage.reference.child(links[1])

        val images: MutableList<Uri?> = mutableListOf()


        withContext(Dispatchers.IO) {
            try {
                firstImageRef.downloadUrl.addOnSuccessListener {
                    images.add(it)
                }.await()
            } catch (e: StorageException) {
                images.add(null)
            }

            try {
                secondImageRef.downloadUrl.addOnSuccessListener {
                    images.add(it)
                }.await()
            } catch (e: StorageException) {
                images.add(null)
            }

        }


        Log.i("images", "images: $images")
        return images
    }

    suspend fun postUserIcon(user: Int, uri: Uri) {
        var path = ""
        var coupleId = ""

        val email = getMailOrName(MAIL)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)

        withContext(Dispatchers.IO) {
            val userData = db.collection(USERS).document(encodedMail).get().await()
            coupleId = userData.get("coupleId") as String
        }

        when (user) {
            1 -> path = "couples/$coupleId/firstPersonIcon.jpg"
            2 -> path = "couples/$coupleId/secondPersonIcon.jpg"
        }

        Log.i("FirestoreRepository", "path: $path")

        val imageRef = storage.reference.child(path)
        val uploadTask = imageRef.putFile(uri)

        Log.i("FirestoreRepository", "uploaded file, task: $uploadTask")
    }

    private fun getMailOrName(choice: Int): String {
        when (choice) {
            NAME -> return getCurrentUser()?.displayName ?: throw Exception(NO_LOGIN_ERROR)
            MAIL -> return getCurrentUser()?.email ?: throw Exception(NO_LOGIN_ERROR)
            ID -> return getCurrentUser()?.uid ?: throw Exception(NO_LOGIN_ERROR)
        }
        return ""
    }

    /**
     * This function pushes the logged in user his username and email
     * to the firestore database with isTaken to false and a 0 coupleid
     */
    suspend fun createUser() {
        val username = getMailOrName(NAME)
        val email = getMailOrName(MAIL)
        withContext(Dispatchers.IO) {
            val user = User(email, username, false, "null")
            val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
            db.collection(USERS).document(encodedMail).set(user).await()
        }
    }

    /**
     * This function checks the isTaken variable of a specific user one time
     * and returns this variable.
     *
     * @return the isTaken field from firestore of type Boolean
     */
    suspend fun checkIsTakenOnce(): Boolean {
        var isTaken = false
        val email = getMailOrName(MAIL)
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


    /**
     * This function will observe the isTaken variable in a user his firestore
     * database. When the variable changes it will return.
     *
     * @return the isTaken field from firestore of type Boolean
     */
    fun isTaken(): Flow<Boolean> {
        val email = getMailOrName(MAIL)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        return db.collection(USERS)
            .document(encodedMail)
            .snapshots().map { snapshot ->
                snapshot.get("taken") as Boolean
            }
    }

    /**
     * This function checks the requests collection of a user and will
     * return a new list of Requests every time a request gets added
     *
     * @return the list of requests
     */
    fun getAllRequests(): Flow<List<Request>> {
        val email = getMailOrName(MAIL)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        return db.collection(USERS)
            .document(encodedMail)
            .collection(REQUESTS)
            .snapshots().map { snapshot ->
                snapshot.toObjects(Request::class.java)
            }
    }

    suspend fun getCoupleData(): Couple? {
        val email = getMailOrName(MAIL)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        var data: Couple? = null
        withContext(Dispatchers.IO) {
            val user = db.collection(USERS).document(encodedMail).get().await()
            val coupleId = user.get("coupleId") as String
            if (coupleId != "null") {
                val coupleData = db.collection(COUPLES).document(coupleId).get().await()
                data = Couple(
                    coupleData.get("firstPerson") as String,
                    coupleData.get("secondPerson") as String,
                    coupleData.get("date") as List<Int>
                )
            }
        }
        return data
    }


    /**
     * This function adds a request to the corresponding lovers firestore location
     *
     * @param email the email of the lover
     * @param date the date when the lover and user got together
     *
     * @return an error string if needed
     */
    suspend fun addLover(email: String, date: List<Int>): String? {
        var returnValue: String?
        val currentUserEmail = getMailOrName(MAIL)
        val currentUsername = getMailOrName(NAME)
        val encodedMail = Base64.encodeToString(currentUserEmail.toByteArray(), Base64.DEFAULT)
        val encodedLoverMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)

        if (email == currentUserEmail) {
            returnValue = "You cannot add yourself."
            return returnValue
        }

        withContext(Dispatchers.IO) {

            val loverAccount = db.collection(USERS).document(encodedLoverMail).get().await()
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
            } else if (isTaken) {
                returnValue = "User is already taken."
            } else {
                returnValue = "User does not exist."
            }
        }
        return returnValue
    }

    /**
     * This function declines a request by removing it from the requests collection
     *
     * @param request object of the corresponding request
     */
    suspend fun declineRequest(request: Request) {
        val currentUserEmail = getMailOrName(MAIL)
        val encodedMail = Base64.encodeToString(currentUserEmail.toByteArray(), Base64.DEFAULT)
        val encodedLoverMail = Base64.encodeToString(request.email.toByteArray(), Base64.DEFAULT)
        withContext(Dispatchers.IO) {
            db.collection(USERS).document(encodedMail)
                .collection(REQUESTS).document(encodedLoverMail)
                .delete()
                .await()
        }
    }

    /**
     * This function accepts a request by creating a couples document and
     * updating their coupleid and istaken variable. And it also deletes the requests if
     * there are other pending requests left.
     *
     * @param request object of the corresponding request
     */
    suspend fun acceptRequest(request: Request) {
        val currentUserEmail = getMailOrName(MAIL)
        val currentUsername = getMailOrName(NAME)

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
        val email = getMailOrName(MAIL)
        val encodedMail = Base64.encodeToString(email.toByteArray(), Base64.DEFAULT)
        return withContext(Dispatchers.IO) {
            db.collection(USERS).document(encodedMail).delete().await()
        }
    }
}

