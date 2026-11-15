package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils
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
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val state = _uiState.value
        val trimmedInput = state.emailOrPhone.trim()

        if (trimmedInput.isEmpty()) {
            val error = "Vui lòng nhập email, số điện thoại hoặc tên đăng nhập"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (state.detectedInputType == InputType.UNKNOWN) {
            val error = "Định dạng đăng nhập không hợp lệ"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (state.password.isEmpty()) {
            val error = "Vui lòng nhập mật khẩu"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isLoginSuccess = true, errorMessage = null) }
        onSuccess(trimmedInput)
    }

    fun register(
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val state = _uiState.value
        val trimmedInput = state.emailOrPhone.trim()

        if (state.fullName.trim().isEmpty()) {
            val error = "Vui lòng nhập họ và tên"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (trimmedInput.isEmpty()) {
            val error = "Vui lòng nhập email hoặc số điện thoại"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (state.detectedInputType == InputType.UNKNOWN) {
            val error = "Email hoặc số điện thoại không hợp lệ"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        if (state.password.length < 6) {
            val error = "Mật khẩu phải có ít nhất 6 ký tự"
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

        if (!state.isAgreementAccepted) {
            val error = "Vui lòng đồng ý với điều khoản sử dụng"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(isRegisterSuccess = true, errorMessage = null) }
        onSuccess(trimmedInput)
    }

    fun sendForgotPasswordOtp(
        destination: String = "",
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val target = destination.ifEmpty { _uiState.value.emailOrPhone }.trim()
        val type = InputTypeUtils.determineEmailOrPhone(target)

        if (type == InputType.UNKNOWN) {
            val error = "Vui lòng nhập email hoặc số điện thoại hợp lệ"
            _uiState.update { it.copy(errorMessage = error) }
            onError(error)
            return
        }

        _uiState.update { it.copy(otpTarget = target, errorMessage = null) }
        startResendCountdown()
        onSuccess(target)
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
