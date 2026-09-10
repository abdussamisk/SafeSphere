package com.example.safesphere.Authy

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

data class GoogleAuthUser(
    val idToken: String,
    val email: String,
    val displayName: String?,
    val profilePictureUrl: String?
)

object GoogleAuthHelper {
    /**
     * Replace with your actual Web Client ID from Google Cloud Console when available.
     * Example: "1234567890-abcdef.apps.googleusercontent.com"
     */
    const val WEB_CLIENT_ID = "531856811897-8q55j6333092n4qambp7rjvaoh7f2gjl.apps.googleusercontent.com"

    suspend fun signInWithGoogle(context: Context): Result<GoogleAuthUser> {
        // If WEB_CLIENT_ID is still the default placeholder, fallback to demo user with notice
        if (WEB_CLIENT_ID.startsWith("YOUR_WEB_CLIENT_ID")) {
            Toast.makeText(
                context,
                "Using Demo Google Sign-In (Replace WEB_CLIENT_ID in GoogleAuthHelper.kt with your Google Cloud Web Client ID for production)",
                Toast.LENGTH_LONG
            ).show()

            return Result.success(
                GoogleAuthUser(
                    idToken = "demo_google_id_token_${System.currentTimeMillis()}",
                    email = "google.user@example.com",
                    displayName = "Google User",
                    profilePictureUrl = null
                )
            )
        }

        return try {
            val credentialManager = CredentialManager.create(context)

            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

            val user = GoogleAuthUser(
                idToken = googleIdTokenCredential.idToken,
                email = googleIdTokenCredential.id,
                displayName = googleIdTokenCredential.displayName,
                profilePictureUrl = googleIdTokenCredential.profilePictureUri?.toString()
            )
            Result.success(user)
        } catch (e: GetCredentialCancellationException) {
            Log.d("GoogleAuthHelper", "User cancelled Google Sign-In")
            Result.failure(e)
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleAuthHelper", "Invalid Google ID token response", e)
            Toast.makeText(context, "Google Token Parsing Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            Result.failure(e)
        } catch (e: GetCredentialException) {
            val errorDetails = "${e.javaClass.simpleName}: ${e.message}"
            Log.e("GoogleAuthHelper", "Credential Manager exception: $errorDetails", e)
            Toast.makeText(context, "Google Auth Error: ${e.message ?: e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            Result.failure(e)
        } catch (e: Exception) {
            val errorDetails = "${e.javaClass.simpleName}: ${e.message}"
            Log.e("GoogleAuthHelper", "Google Sign-In error: $errorDetails", e)
            Toast.makeText(context, "Google Sign-In Error: ${e.message ?: e.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            Result.failure(e)
        }


    }
}

