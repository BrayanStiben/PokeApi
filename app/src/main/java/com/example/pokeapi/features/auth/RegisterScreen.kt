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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trainerName by viewModel.trainerName
    val trainerId by viewModel.trainerId
    val password by viewModel.password
    val confirmPassword by viewModel.confirmPassword
    val agreeToTerms by viewModel.agreeToTerms
    val isLoading by viewModel.isLoading
    
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.RegisterSuccess -> {
                    Toast.makeText(context, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                    onRegisterSuccess()
                }
                is AuthUiEvent.ShowError -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    AuthBackground(title = "NUEVO PERFIL", onBackClick = onBackClick) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RegisterField("NOMBRE DEL ENTRENADOR", trainerName, viewModel::onTrainerNameChange, "ej., Misty", Icons.Default.Person)
            RegisterField("ID DE ENTRENADOR", trainerId, viewModel::onTrainerIdChange, "ej., #AB1234567", Icons.Default.Badge)

            Column {
                Text("CONTRASEÑA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
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
            }

            Column {
                Text("CONFIRMAR CONTRASEÑA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = Color(0xFFE3350D)) },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    colors = authTextFieldColors(),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeToTerms, 
                    onCheckedChange = viewModel::onAgreeChange,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFE3350D))
                )
                Text("ACEPTO TÉRMINOS Y CONDICIONES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }

            Button(
                onClick = viewModel::onRegisterClick,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3350D)),
                shape = RoundedCornerShape(27.dp),
                enabled = !isLoading,
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.CatchingPokemon, null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("REGISTRARSE", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun RegisterField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            leadingIcon = { Icon(icon, null, tint = Color(0xFFE3350D)) },
            shape = RoundedCornerShape(12.dp),
            colors = authTextFieldColors(),
            singleLine = true
        )
    }
}
