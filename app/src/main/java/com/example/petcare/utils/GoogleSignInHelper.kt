package com.example.petcare.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.petcare.R
import com.example.petcare.ui.viewmodel.AuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object GoogleSignInHelper {

    private fun Context.findActivity(): Activity? {
        var current = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    fun launchGoogleSignIn(
        context: Context,
        coroutineScope: CoroutineScope,
        viewModel: AuthViewModel,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val targetContext = context.findActivity() ?: context

        val serverClientId = runCatching {
            context.getString(R.string.default_web_client_id)
        }.getOrNull()?.ifEmpty { null }
            ?: "128631296492-1q8qcofdj7cjochg47vt2geenomobk0p.apps.googleusercontent.com"

        val credentialManager = CredentialManager.create(targetContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
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
                    val msg = "Không nhận được chứng chỉ Google hợp lệ"
                    Log.w("GoogleSignInHelper", msg)
                    onError(msg)
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d("GoogleSignInHelper", "Người dùng đã hủy chọn tài khoản Google")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Đăng nhập Google thất bại"
                Log.e("GoogleSignInHelper", "Google Sign-in error: $errorMsg", e)
                onError(errorMsg)
            }
        }
    }
}
