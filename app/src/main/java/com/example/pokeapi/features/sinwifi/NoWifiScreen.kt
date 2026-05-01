package com.example.pokeapi.features.sinwifi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NoWifiScreen(
    pokemonName: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    viewModel: NoWifiViewModel = hiltViewModel()
) {
    LaunchedEffect(pokemonName) {
        viewModel.setPokemonName(pokemonName)
    }
    
    val name by viewModel.pokemonName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1E6)) // Beige claro de fondo
    ) {
        // Cabecera POKÉMON CENTER con botón de volver
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF3B7CBE))
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).padding(top = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Row(
                modifier = Modifier.align(Alignment.Center).padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PokeballIcon(size = 28.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "POKÉMON CENTER",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // Contenido Principal (Tarjeta de Error)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "ERROR DE CONEXIÓN",
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Icono de Wifi con X roja
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            modifier = Modifier.size(90.dp),
                            tint = Color(0xFFD32F2F)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "No se pudieron obtener los datos de\n$name Pokémon.",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.Black,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "POR FAVOR REVISE SU CONEXIÓN E\nINTENTE NUEVAMENTE.",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(35.dp))

                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A7A99)),
                        shape = RoundedCornerShape(26.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text(
                            "REINTENTAR CONEXIÓN",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PokeballIcon(size: androidx.compose.ui.unit.Dp, hasBorder: Boolean = false) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = Color.White,
        border = if (hasBorder) androidx.compose.foundation.BorderStroke(2.dp, Color.Black) else null
    ) {
        Column {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFE53935))) // Rojo Pokeball
            Box(modifier = Modifier.height(if (size > 30.dp) 3.dp else 2.dp).fillMaxWidth().background(Color.Black))
            Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White))
        }
        // Centro de la pokeball
        Box(contentAlignment = Alignment.Center) {
             Surface(
                modifier = Modifier.size(if (size > 30.dp) 12.dp else 8.dp),
                shape = CircleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(if (size > 30.dp) 2.dp else 1.dp, Color.Black)
            ) {}
        }
    }
}
