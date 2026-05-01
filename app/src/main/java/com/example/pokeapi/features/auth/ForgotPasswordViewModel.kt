package com.example.pokeapi.features.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokeapi.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _trainerId = mutableStateOf("")
    val trainerId: State<String> = _trainerId

    private val _newPassword = mutableStateOf("")
    val newPassword: State<String> = _newPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTrainerIdChange(value: String) { _trainerId.value = value }
    fun onNewPasswordChange(value: String) { _newPassword.value = value }

    fun onResetClick() {
        if (_trainerId.value.isBlank() || _newPassword.value.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(AuthUiEvent.ShowError("Por favor, completa todos los campos")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.resetPassword(_trainerId.value, _newPassword.value)
                .onSuccess {
                    _uiEvent.emit(AuthUiEvent.PasswordResetSuccess)
                }
                .onFailure {
                    _uiEvent.emit(AuthUiEvent.ShowError(it.message ?: "Error al restablecer la contraseña"))
                }
            _isLoading.value = false
        }
    }
}
