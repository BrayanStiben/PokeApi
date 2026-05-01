package com.example.pokeapi.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthBackground(
    title: String,
    onBackClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val pokeRed = Color(0xFFE3350D)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Fondo rojo superior que ocupa la mitad de la pantalla para dar peso visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(pokeRed)
                .statusBarsPadding()
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás", tint = Color.White)
                }
            }
            
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp), // Bajado un poco más para que esté más centrado visualmente en el área roja
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CatchingPokemon,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp) // Más grande
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "CENTRO POKÉMON",
                    color = Color.White,
                    fontSize = 26.sp, // Más grande
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }
        }

        // Tarjeta totalmente centrada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp), // Desplazamiento hacia abajo del centro absoluto
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(28.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = pokeRed,
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 18.dp),
                        thickness = 1.5.dp,
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Color(0xFFE3350D),
    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
    cursorColor = Color(0xFFE3350D),
    focusedLabelColor = Color(0xFFE3350D),
    unfocusedLabelColor = Color.Gray
)
