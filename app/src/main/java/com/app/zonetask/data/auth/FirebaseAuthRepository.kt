package com.app.zonetask.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class FirebaseSessionUser(
    val uid: String,
    val email: String
)

class FirebaseAuthRepository(
    private val authProvider: () -> FirebaseAuth? = {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
) {

    fun currentSessionUser(): FirebaseSessionUser? {
        return authProvider()?.currentUser?.toSessionUser()
    }

    fun signOut() {
        authProvider()?.signOut()
    }

    suspend fun signIn(
        email: String,
        password: String
    ): Result<FirebaseSessionUser> {
        val auth = authProvider()
            ?: return Result.failure(IllegalStateException("Firebase no está configurado en este dispositivo."))

        return suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val sessionUser = result.user?.toSessionUser()
                    if (sessionUser != null) {
                        continuation.resume(Result.success(sessionUser))
                    } else {
                        continuation.resume(
                            Result.failure(
                                IllegalStateException("Firebase returned an empty user.")
                            )
                        )
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resume(Result.failure(error))
                }
        }
    }

    private fun FirebaseUser.toSessionUser(): FirebaseSessionUser {
        return FirebaseSessionUser(
            uid = uid,
            email = email.orEmpty()
        )
    }
}
