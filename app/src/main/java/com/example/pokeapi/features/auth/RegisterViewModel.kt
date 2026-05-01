package com.example.pokeapi.features.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokeapi.data.model.UserEntity
import com.example.pokeapi.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _trainerName = mutableStateOf("")
    val trainerName: State<String> = _trainerName

    private val _trainerId = mutableStateOf("")
    val trainerId: State<String> = _trainerId

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _agreeToTerms = mutableStateOf(false)
    val agreeToTerms: State<Boolean> = _agreeToTerms

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTrainerNameChange(value: String) { _trainerName.value = value }
    fun onTrainerIdChange(value: String) { _trainerId.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun onConfirmPasswordChange(value: String) { _confirmPassword.value = value }
    fun onAgreeChange(value: Boolean) { _agreeToTerms.value = value }

    fun onRegisterClick() {
        if (_trainerName.value.isBlank() || _trainerId.value.isBlank() || 
            _password.value.isBlank() || _confirmPassword.value.isBlank()) {
            viewModelScope.launch { _uiEvent.emit(AuthUiEvent.ShowError("Por favor, completa todos los campos")) }
            return
        }

        if (_password.value != _confirmPassword.value) {
            viewModelScope.launch { _uiEvent.emit(AuthUiEvent.ShowError("Las contraseñas no coinciden")) }
            return
        }

        if (!_agreeToTerms.value) {
            viewModelScope.launch { _uiEvent.emit(AuthUiEvent.ShowError("Por favor, acepta los términos y condiciones")) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val user = UserEntity(
                trainerId = _trainerId.value,
                trainerName = _trainerName.value,
                password = _password.value,
                isLogged = false
            )
            repository.register(user)
                .onSuccess {
                    _uiEvent.emit(AuthUiEvent.RegisterSuccess)
                }
                .onFailure {
                    _uiEvent.emit(AuthUiEvent.ShowError(it.message ?: "Error al registrarse"))
                }
            _isLoading.value = false
        }
    }
}
