package com.example.petcare.firebase

import android.util.Log
import com.example.petcare.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

/**
 * Logs structured Firestore operation diagnostics without leaking auth details in release builds.
 */
fun handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?
): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val currentUser = if (BuildConfig.DEBUG) {
        runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
    } else {
        null
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        if (BuildConfig.DEBUG && currentUser != null) {
            val providerInfoList = currentUser.providerData.map { provider ->
                JSONObject().apply {
                    put("providerId", provider.providerId)
                    put("email", provider.email)
                }
            }
            val authInfoJson = JSONObject().apply {
                put("userId", currentUser.uid)
                put("email", currentUser.email)
                put("emailVerified", currentUser.isEmailVerified)
                put("isAnonymous", currentUser.isAnonymous)
                put("providerInfo", JSONArray(providerInfoList))
            }
            put("authInfo", authInfoJson)
        }
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "[ERROR] Firestore Error: $jsonString")
    return jsonString
}
