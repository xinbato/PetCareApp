package com.example.petcare.ui.viewmodel

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.utils.GoogleSignInHelper
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils
import com.example.petcare.utils.NotificationHelper
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class AuthUiState(
    val emailOrPhone: String = "",
    val detectedInputType: InputType = InputType.UNKNOWN,
    val password: String = "",
    val confirmPassword: String = "",
    val fullName: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isAgreementAccepted: Boolean = false,
    val otpCode: String = "",
    val otpTarget: String = "",
    val resendCountdown: Int = 60,
    val canResendOtp: Boolean = false,
    val isLoading: Boolean = false,
    val isCheckingAutoSignIn: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val isOtpVerified: Boolean = false,
    val isPasswordResetSuccess: Boolean = false,
)

class AuthViewModel : ViewModel() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onEmailOrPhoneChanged(input: String) {
        val detected = InputTypeUtils.determineInputType(input)
        _uiState.update {
            it.copy(
                emailOrPhone = input,
                detectedInputType = detected,
                errorMessage = null,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPassword = password, errorMessage = null) }
    }

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun toggleAgreement(accepted: Boolean) {
        _uiState.update { it.copy(isAgreementAccepted = accepted, errorMessage = null) }
    }

    fun onOtpCodeChanged(code: String) {
        _uiState.update { it.copy(otpCode = code, errorMessage = null) }
    }

    fun setOtpTarget(target: String) {
        _uiState.update { it.copy(otpTarget = target) }
        startResendCountdown()
    }

    fun tryGoogleAutoSignIn(
        context: Context,
        onAutoSignedIn: (String) -> Unit = {},
    ) {
        if (GoogleSignInHelper.suppressAutoSignIn || runCatching { auth.currentUser }.getOrNull() != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAutoSignIn = true) }
            val idToken = GoogleSignInHelper.attemptAutoSignIn(context)
            if (idToken != null) {
                signInWithGoogle(
                    idToken = idToken,
                    onSuccess = { name ->
                        _uiState.update { it.copy(isCheckingAutoSignIn = false) }
                        onAutoSignedIn(name)
                    },
                    onError = {
                        _uiState.update { it.copy(isCheckingAutoSignIn = false) }
                    },
                )
            } else {
                _uiState.update { it.copy(isCheckingAutoSignIn = false) }
            }
        }
    }

    fun signInAsGuest(
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        GoogleSignInHelper.suppressAutoSignIn = false
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid.orEmpty()
                if (uid.isNotEmpty()) {
                    val userData = mapOf(
                        "userId" to uid,
                        "name" to "Khách",
                        "email" to "",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    firestore.collection("users").document(uid)
                        .set(userData, SetOptions.merge())
                }
                PetRepository.updateUserProfile(name = "Khách", email = "", isAnonymous = true)
                PetRepository.attachUser(uid)
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                onSuccess("Khách")
            }
            .addOnFailureListener { e ->
                val error = mapAuthException(e, "Đăng nhập nhanh thất bại")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                onError(error)
            }
    }

    fun linkGuestToEmail(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val user = auth.currentUser
        if (user == null || !user.isAnonymous) {
            onError("Chỉ tài khoản Khách mới có thể liên kết.")
            return
        }
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        when {
            cleanName.isEmpty() -> { onError("Vui lòng nhập tên hiển thị."); return }
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> { onError("Địa chỉ email không hợp lệ."); return }
            password.length < 6 -> { onError("Mật khẩu phải có ít nhất 6 ký tự."); return }
            password != confirmPassword -> { onError("Mật khẩu xác nhận không khớp."); return }
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val credential = EmailAuthProvider.getCredential(cleanEmail, password)
        user.linkWithCredential(credential)
            .addOnSuccessListener {
                user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(cleanName).build())
                val updates = mapOf(
                    "userId" to user.uid,
                    "name" to cleanName,
                    "email" to cleanEmail,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                firestore.collection("users").document(user.uid).set(updates, SetOptions.merge())
                PetRepository.updateUserProfile(name = cleanName, email = cleanEmail, isAnonymous = false)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }
            .addOnFailureListener { e ->
                val error = mapAuthException(e, "Không thể liên kết tài khoản.")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                onError(error)
            }
    }

    fun login(
        emailOrPhoneInput: String? = null,
        passwordInput: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val state = _uiState.value
        val trimmedInput = (emailOrPhoneInput ?: state.emailOrPhone).trim()
        val pass = (passwordInput ?: state.password).trim()

        if (trimmedInput.isEmpty()) {
            val error = "Vui lòng nhập email hoặc tên đăng nhập"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (pass.isEmpty()) {
            val error = "Vui lòng nhập mật khẩu"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        if (trimmedInput.contains("@")) {
            signInWithResolvedEmail(trimmedInput, pass, trimmedInput, onSuccess, onError)
        } else {
            val normalizedUsername = trimmedInput.lowercase()
            firestore.collection("usernames").document(normalizedUsername).get()
                .addOnSuccessListener { doc ->
                    val email = doc.getString("email")
                    if (!email.isNullOrBlank()) {
                        signInWithResolvedEmail(email, pass, trimmedInput, onSuccess, onError)
                    } else {
                        val error = "Không tìm thấy tài khoản với tên đăng nhập này. Vui lòng thử dùng email."
                        _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                        onError(error)
                    }
                }
                .addOnFailureListener { e ->
                    val error = mapAuthException(e, "Không thể kết nối đến máy chủ")
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    onError(error)
                }
        }
    }

    private fun signInWithResolvedEmail(
        email: String,
        password: String,
        fallbackName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        GoogleSignInHelper.suppressAutoSignIn = false
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                val displayName = user?.displayName?.ifBlank { null }
                    ?: user?.email?.substringBefore("@")
                    ?: fallbackName
                PetRepository.updateUserProfile(name = displayName, email = user?.email, isAnonymous = false)
                PetRepository.attachUser(user?.uid)
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                onSuccess(displayName)
            }
            .addOnFailureListener { e ->
                val error = mapAuthException(e, "Đăng nhập thất bại")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                onError(error)
            }
    }

    fun register(
        emailOrPhoneInput: String? = null,
        passwordInput: String? = null,
        fullNameInput: String? = null,
        usernameInput: String? = null,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val state = _uiState.value
        val name = (fullNameInput ?: state.fullName).trim()
        val trimmedInput = (emailOrPhoneInput ?: state.emailOrPhone).trim()
        val pass = (passwordInput ?: state.password).trim()
        val username = usernameInput?.trim()?.lowercase().orEmpty()

        if (name.isEmpty()) {
            val error = "Vui lòng nhập họ và tên"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedInput).matches()) {
            val error = "Vui lòng đăng ký bằng địa chỉ email hợp lệ"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (pass.length < 6) {
            val error = "Mật khẩu phải có ít nhất 6 ký tự"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        GoogleSignInHelper.suppressAutoSignIn = false

        auth.createUserWithEmailAndPassword(trimmedInput, pass)
            .addOnSuccessListener { result ->
                val user = result.user
                val uid = user?.uid.orEmpty()
                if (user != null && name.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates)
                }
                if (uid.isNotEmpty()) {
                    val userData = mutableMapOf<String, Any>(
                        "userId" to uid,
                        "name" to name.take(80),
                        "email" to trimmedInput.take(120),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    if (username.isNotEmpty()) {
                        userData["username"] = username.take(40)
                        if (Regex("^[a-z0-9._]{3,40}$").matches(username)) {
                            firestore.collection("usernames").document(username).set(
                                mapOf(
                                    "userId" to uid,
                                    "email" to trimmedInput.take(120),
                                    "updatedAt" to FieldValue.serverTimestamp(),
                                )
                            )
                        }
                    }
                    firestore.collection("users").document(uid).set(userData, SetOptions.merge())
                }
                PetRepository.updateUserProfile(name = name, email = trimmedInput, isAnonymous = false)
                PetRepository.attachUser(uid)
                _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                onSuccess(name)
            }
            .addOnFailureListener { e ->
                val error = mapAuthException(e, "Đăng ký thất bại")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                onError(error)
            }
    }

    fun sendForgotPasswordOtp(
        destination: String = "",
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val target = destination.ifEmpty { _uiState.value.emailOrPhone }.trim()
        if (target.isEmpty()) {
            val error = "Vui lòng nhập email hoặc tên đăng nhập"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        if (target.contains("@")) {
            auth.sendPasswordResetEmail(target)
                .addOnSuccessListener {
                    _uiState.update { it.copy(isLoading = false, otpTarget = target) }
                    startResendCountdown()
                    onSuccess(target)
                }
                .addOnFailureListener { e ->
                    val error = mapAuthException(e, "Không thể gửi email khôi phục mật khẩu")
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    onError(error)
                }
        } else {
            firestore.collection("usernames").document(target.lowercase()).get()
                .addOnSuccessListener { doc ->
                    val email = doc.getString("email")
                    if (!email.isNullOrBlank()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                _uiState.update { it.copy(isLoading = false, otpTarget = email) }
                                startResendCountdown()
                                onSuccess(email)
                            }
                            .addOnFailureListener { e ->
                                val error = mapAuthException(e, "Không thể gửi email khôi phục mật khẩu")
                                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                                onError(error)
                            }
                    } else {
                        val error = "Không tìm thấy tài khoản với tên đăng nhập này"
                        _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                        onError(error)
                    }
                }
                .addOnFailureListener { e ->
                    val error = mapAuthException(e, "Lỗi kết nối cơ sở dữ liệu")
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    onError(error)
                }
        }
    }

    fun sendVerificationEmail(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("Chưa đăng nhập tài khoản")
            return
        }

        user.sendEmailVerification()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(mapAuthException(e, "Không thể gửi email xác thực"))
            }
    }

    fun logout(
        context: Context? = null,
        onLoggedOut: () -> Unit = {},
    ) {
        GoogleSignInHelper.suppressAutoSignIn = true
        context?.let { NotificationHelper.clearAllReminders(it) }
        auth.signOut()
        PetRepository.detachUser()
        resetState()
        if (context == null) {
            onLoggedOut()
            return
        }
        val appContext = context.applicationContext
        viewModelScope.launch {
            withContext(NonCancellable) {
                withTimeoutOrNull(1500L) {
                    GoogleSignInHelper.clearCredentialState(appContext)
                }
            }
            onLoggedOut()
        }
    }

    fun deleteAccount(
        context: Context? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val user = auth.currentUser
        if (user == null) {
            logout(context, onSuccess)
            return
        }
        val uid = user.uid
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { PetRepository.deleteUserCloudData(uid) }
            user.delete()
                .addOnSuccessListener {
                    _uiState.update { it.copy(isLoading = false) }
                    logout(context, onSuccess)
                }
                .addOnFailureListener { e ->
                    val err = mapAuthException(e, "Không thể xóa tài khoản. Vui lòng đăng nhập lại trước khi xóa.")
                    _uiState.update { it.copy(isLoading = false, errorMessage = err) }
                    onError(err)
                }
        }
    }

    fun signInWithGoogle(
        idToken: String,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        GoogleSignInHelper.suppressAutoSignIn = false
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                val displayName = user?.displayName?.ifBlank { null }
                    ?: user?.email?.substringBefore("@")
                    ?: "Bạn"
                val email = user?.email.orEmpty()
                val uid = user?.uid.orEmpty()
                val photoUrl = user?.photoUrl?.toString()
                val phone = user?.phoneNumber

                if (uid.isNotEmpty()) {
                    val userData = mutableMapOf<String, Any>(
                        "userId" to uid,
                        "name" to displayName.take(80),
                        "email" to email.take(120),
                        "lastLogin" to System.currentTimeMillis(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                    if (photoUrl != null) userData["avatarUri"] = photoUrl.take(600)
                    if (!phone.isNullOrBlank()) userData["phone"] = phone.take(30)

                    firestore.collection("users").document(uid).set(userData, SetOptions.merge())
                }
                PetRepository.updateUserProfile(
                    name = displayName,
                    email = email,
                    avatarUri = photoUrl,
                    phone = phone,
                    isAnonymous = false,
                )
                PetRepository.attachUser(uid)
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                onSuccess(displayName)
            }
            .addOnFailureListener { e ->
                val error = mapAuthException(e, "Đăng nhập Google thất bại")
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                onError(error)
            }
    }

    fun verifyOtp(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val code = _uiState.value.otpCode.trim()
        if (code.length < 4) {
            val error = "Mã OTP phải có ít nhất 4 chữ số"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isOtpVerified = true, errorMessage = null) }
        onSuccess()
    }

    fun resetPassword(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val state = _uiState.value
        if (state.password.length < 6) {
            val error = "Mật khẩu mới phải có ít nhất 6 ký tự"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (state.password != state.confirmPassword) {
            val error = "Mật khẩu xác nhận không khớp"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isPasswordResetSuccess = true, errorMessage = null) }
        onSuccess()
    }

    fun startResendCountdown(seconds: Int = 60) {
        countdownJob?.cancel()
        _uiState.update { it.copy(resendCountdown = seconds, canResendOtp = false) }
        countdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(resendCountdown = remaining) }
            }
            _uiState.update { it.copy(canResendOtp = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetState() {
        countdownJob?.cancel()
        _uiState.value = AuthUiState()
    }

    private fun mapAuthException(e: Exception, fallback: String): String {
        if (e is FirebaseAuthException) {
            return when (e.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Phương thức đăng nhập này chưa được bật trong Firebase Console."
                "ERROR_INVALID_EMAIL" -> "Địa chỉ email không đúng định dạng."
                "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                    "Email hoặc mật khẩu không chính xác."
                "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                    "Email này đã được sử dụng cho tài khoản khác."
                "ERROR_WEAK_PASSWORD" -> "Mật khẩu quá yếu. Vui lòng chọn từ 6 ký tự trở lên."
                "ERROR_USER_DISABLED" -> "Tài khoản này đã bị vô hiệu hóa."
                "ERROR_TOO_MANY_REQUESTS" -> "Bạn thử quá nhiều lần. Vui lòng đợi ít phút rồi thử lại."
                "ERROR_REQUIRES_RECENT_LOGIN" -> "Vui lòng đăng nhập lại trước khi thực hiện thao tác này."
                else -> e.localizedMessage ?: fallback
            }
        }
        return e.localizedMessage ?: fallback
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
