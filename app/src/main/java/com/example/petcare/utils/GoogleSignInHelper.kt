package com.example.petcare.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.petcare.R
import com.example.petcare.ui.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object GoogleSignInHelper {
    private const val TAG = "GoogleSignInHelper"

    @Volatile
    var suppressAutoSignIn: Boolean = false

    private fun Context.findActivity(): Activity? {
        var current = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun resolveServerClientId(context: Context): String? {
        return runCatching {
            context.getString(R.string.default_web_client_id)
        }.getOrNull()?.ifBlank { null }
    }

    suspend fun attemptAutoSignIn(context: Context): String? {
        if (suppressAutoSignIn) return null
        val clientId = resolveServerClientId(context) ?: return null
        val targetContext = context.findActivity() ?: context
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val result = CredentialManager.create(targetContext).getCredential(targetContext, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleIdTokenCredential.createFrom(cred.data).idToken
            } else {
                null
            }
        } catch (_: NoCredentialException) {
            null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearCredentialState(context: Context) {
        runCatching {
            CredentialManager.create(context.applicationContext)
                .clearCredentialState(ClearCredentialStateRequest())
        }
    }

    fun launchGoogleSignIn(
        context: Context,
        coroutineScope: CoroutineScope,
        viewModel: AuthViewModel,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        suppressAutoSignIn = false
        val targetContext = context.findActivity() ?: context
        val serverClientId = resolveServerClientId(context)
        if (serverClientId == null) {
            onError("Thiếu cấu hình đăng nhập Google (default_web_client_id).")
            return
        }

        val credentialManager = CredentialManager.create(targetContext)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(context = targetContext, request = request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.signInWithGoogle(
                        idToken = googleIdTokenCredential.idToken,
                        onSuccess = onSuccess,
                        onError = onError,
                    )
                } else {
                    val msg = "Không nhận được chứng chỉ Google hợp lệ."
                    Log.w(TAG, msg)
                    onError(msg)
                }
            } catch (_: GetCredentialCancellationException) {
                Log.d(TAG, "Google Sign-In cancelled by user")
            } catch (e: NoCredentialException) {
                val msg = "Không tìm thấy tài khoản Google trên thiết bị. Vui lòng thêm tài khoản Google trong Cài đặt hoặc đăng nhập bằng Email."
                Log.w(TAG, msg, e)
                onError(msg)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("No credentials", ignoreCase = true) == true) {
                    "Không tìm thấy tài khoản Google trên thiết bị. Vui lòng thêm tài khoản Google trong Cài đặt hoặc đăng nhập bằng Email."
                } else {
                    e.localizedMessage ?: "Đăng nhập Google thất bại"
                }
                Log.e(TAG, "[ERROR] Google Sign-In error: $errorMsg", e)
                onError(errorMsg)
            }
        }
    }
}
