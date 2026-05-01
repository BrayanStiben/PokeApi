package com.example.pokeapi.features.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trainerId by viewModel.trainerId
    val password by viewModel.password
    val isLoading by viewModel.isLoading
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.LoginSuccess -> onLoginSuccess()
                is AuthUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    AuthBackground(title = "PORTAL DE ENTRENADOR") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("USUARIO / ID", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
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
                Text("CONTRASEÑA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = Color(0xFFE3350D)) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = authTextFieldColors(),
                    singleLine = true
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        modifier = Modifier.padding(top = 8.dp).clickable { onNavigateToForgotPassword() },
                        fontSize = 13.sp,
                        color = Color(0xFFE3350D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::onLoginClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3350D)),
                shape = RoundedCornerShape(27.dp),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.AutoMirrored.Filled.Login, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("INICIAR SESIÓN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿No tienes cuenta? ", fontSize = 14.sp, color = Color.Gray)
                Text(
                    "Regístrate",
                    modifier = Modifier.clickable { onNavigateToRegister() },
                    fontSize = 14.sp,
                    color = Color(0xFFE3350D),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

