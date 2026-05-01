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
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _trainerId = mutableStateOf("")
    val trainerId: State<String> = _trainerId

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTrainerIdChange(value: String) {
        _trainerId.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun onLoginClick() {
        if (_trainerId.value.isBlank() || _password.value.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(AuthUiEvent.ShowError("Por favor, completa todos los campos")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.login(_trainerId.value, _password.value)
                .onSuccess {
                    _uiEvent.emit(AuthUiEvent.LoginSuccess)
                }
                .onFailure {
                    _uiEvent.emit(AuthUiEvent.ShowError(it.message ?: "Error al iniciar sesión"))
                }
            _isLoading.value = false
        }
    }
}

sealed class AuthUiEvent {
    object LoginSuccess : AuthUiEvent()
    object RegisterSuccess : AuthUiEvent()
    object PasswordResetSuccess : AuthUiEvent()
    data class ShowError(val message: String) : AuthUiEvent()
}
