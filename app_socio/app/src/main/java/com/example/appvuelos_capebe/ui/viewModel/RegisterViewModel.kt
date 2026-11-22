package com.example.appvuelos_capebe.ui.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appvuelos_capebe.data.database.AppDatabase
import com.example.appvuelos_capebe.data.local.entity.UsuarioEntity
import com.example.appvuelos_capebe.model.RegisterModel
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    var uiState by mutableStateOf(RegisterModel())
        private set

    fun onNameChanged(name: String) {
        uiState = uiState.copy(name = name, errorMessage = null)
    }

    fun onEmailChanged(email: String) {
        uiState = uiState.copy(email = email, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(password = password, errorMessage = null)
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        uiState = uiState.copy(confirmPassword = confirmPassword, errorMessage = null)
    }

    fun onPinChanged(pin: String) {
        val filtered = pin.filter { it.isDigit() }.take(4)
        uiState = uiState.copy(pin = filtered, errorMessage = null)
    }

    fun limpiarFormulario() {
        uiState = RegisterModel()
    }

    // validaciones y registro en SQLite (Room)
    fun onRegisterClick(context: Context, onSuccess: (String) -> Unit) {
        val state = uiState

        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank() || state.pin.isBlank()) {
            uiState = uiState.copy(errorMessage = "Por favor completa todos los campos y tu PIN.")
            return
        }

        if (!state.email.contains("@")) {
            uiState = uiState.copy(errorMessage = "Ingresa un correo válido.")
            return
        }

        if (state.password.length < 8) {
            uiState = uiState.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres.")
            return
        }

        if (state.password != state.confirmPassword) {
            uiState = uiState.copy(errorMessage = "Las contraseñas no coinciden.")
            return
        }

        if (state.pin.length != 4) {
            uiState = uiState.copy(errorMessage = "El PIN de acceso debe tener exactamente 4 dígitos.")
            return
        }

        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val nombreLimpio = state.name.trim()
            val correoLimpio = state.email.trim()

            val newUser = UsuarioEntity(
                name = nombreLimpio,
                email = correoLimpio,
                password = state.password,
                pin = state.pin
            )
            db.usuarioDao().registerUser(newUser)
            uiState = RegisterModel()
            onSuccess(nombreLimpio)
        }
    }
}
