package com.example.pokeapi.features.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ForgotPasswordScreen(
    onResetSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trainerId by viewModel.trainerId
    val newPassword by viewModel.newPassword
    val isLoading by viewModel.isLoading

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.PasswordResetSuccess -> {
                    Toast.makeText(context, "¡Contraseña restablecida!", Toast.LENGTH_SHORT).show()
                    onResetSuccess()
                }
                is AuthUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    AuthBackground(title = "RESTABLECER", onBackClick = onBackClick) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("ID DE ENTRENADOR", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = trainerId,
                    onValueChange = viewModel::onTrainerIdChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ej., Ash.Ketchum", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFFE3350D)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = authTextFieldColors(),
                    singleLine = true
                )
            }

            Column {
                Text("NUEVA CONTRASEÑA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = Color(0xFFE3350D)) },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = authTextFieldColors(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::onResetClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3350D)),
                shape = RoundedCornerShape(27.dp),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("ACTUALIZAR CONTRASEÑA", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}
