package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
            val error = "Vui lòng nhập email hoặc tài khoản"
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
            // Đăng nhập trực tiếp bằng Email
            auth.signInWithEmailAndPassword(trimmedInput, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val displayName = user?.displayName?.ifBlank { null }
                        ?: user?.email?.substringBefore("@")
                        ?: trimmedInput
                    PetRepository.updateUserProfile(name = displayName, email = user?.email)
                    PetRepository.attachUser(user?.uid)
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                    onSuccess(displayName)
                }
                .addOnFailureListener { e ->
                    val error = e.localizedMessage ?: "Đăng nhập thất bại"
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    onError(error)
                }
        } else {
            firestore.collection("users")
                .whereEqualTo("username", trimmedInput.lowercase())
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val userDoc = snapshot.documents.firstOrNull()
                    val email = userDoc?.getString("email")
                    if (!email.isNullOrBlank()) {
                        auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener { result ->
                                val user = result.user
                                val displayName = userDoc.getString("name")
                                    ?: user?.displayName?.ifBlank { null }
                                    ?: trimmedInput
                                PetRepository.updateUserProfile(name = displayName, email = email)
                                PetRepository.attachUser(user?.uid)
                                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                                onSuccess(displayName)
                            }
                            .addOnFailureListener { e ->
                                val error = e.localizedMessage ?: "Mật khẩu không chính xác"
                                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                                onError(error)
                            }
                    } else {
                        val error = "Không tìm thấy tài khoản với tên đăng nhập này. Vui lòng thử dùng email."
                        _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                        onError(error)
                    }
                }
                .addOnFailureListener { e ->
                    val error = e.localizedMessage ?: "Không thể kết nối đến máy chủ"
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    onError(error)
                }
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

        if (trimmedInput.isEmpty()) {
            val error = "Vui lòng nhập email"
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

        if (trimmedInput.contains("@")) {
            auth.createUserWithEmailAndPassword(trimmedInput, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val uid = user?.uid.orEmpty()
                    if (uid.isNotEmpty()) {
                        val userData = mutableMapOf<String, Any>(
                            "name" to name,
                            "email" to trimmedInput,
                            "createdAt" to System.currentTimeMillis()
                        )
                        if (username.isNotEmpty()) {
                            userData["username"] = username
                        }
                        firestore.collection("users").document(uid).set(userData)
                    }
                    PetRepository.updateUserProfile(name = name, email = trimmedInput)
                    PetRepository.attachUser(uid)
                    _uiState.update { it.copy(isLoading = false, isRegisterSuccess = true) }
                    onSuccess(name)
                }
                .addOnFailureListener { e ->
                    val error = e.localizedMessage ?: "Đăng ký thất bại"
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                    onError(error)
                }
        } else {
            val error = "Vui lòng đăng ký bằng địa chỉ email hợp lệ"
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
        if (target.isEmpty() || !target.contains("@")) {
            val error = "Vui lòng nhập email hợp lệ để nhận liên kết khôi phục"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        auth.sendPasswordResetEmail(target)
            .addOnSuccessListener {
                _uiState.update { it.copy(isLoading = false, otpTarget = target) }
                startResendCountdown()
                onSuccess(target)
            }
            .addOnFailureListener { e ->
                val error = e.localizedMessage ?: "Không thể gửi email khôi phục mật khẩu"
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                onError(error)
            }
    }

    fun logout() {
        auth.signOut()
        PetRepository.detachUser()
        resetState()
    }

    fun deleteAccount(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val user = auth.currentUser
        if (user == null) {
            PetRepository.detachUser()
            resetState()
            onSuccess()
            return
        }
        val uid = user.uid
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        firestore.collection("users").document(uid).delete()
            .addOnCompleteListener {
                user.delete()
                    .addOnSuccessListener {
                        PetRepository.detachUser()
                        _uiState.update { it.copy(isLoading = false) }
                        resetState()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        val err = e.localizedMessage ?: "Không thể xóa tài khoản. Vui lòng đăng nhập lại trước khi xóa."
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
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                val displayName = user?.displayName?.ifBlank { null }
                    ?: user?.email?.substringBefore("@")
                    ?: "User"
                val email = user?.email.orEmpty()
                val uid = user?.uid.orEmpty()
                val photoUrl = user?.photoUrl?.toString()
                val phone = user?.phoneNumber

                if (uid.isNotEmpty()) {
                    val userData = mutableMapOf<String, Any>(
                        "name" to displayName,
                        "email" to email,
                        "lastLogin" to System.currentTimeMillis()
                    )
                    if (photoUrl != null) userData["avatarUri"] = photoUrl
                    if (!phone.isNullOrBlank()) userData["phone"] = phone

                    firestore.collection("users").document(uid).set(
                        userData,
                        SetOptions.merge()
                    )
                }
                PetRepository.updateUserProfile(
                    name = displayName,
                    email = email,
                    avatarUri = photoUrl,
                    phone = phone
                )
                PetRepository.attachUser(uid)
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                onSuccess(displayName)
            }
            .addOnFailureListener { e ->
                val error = e.localizedMessage ?: "Đăng nhập Google thất bại"
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

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
