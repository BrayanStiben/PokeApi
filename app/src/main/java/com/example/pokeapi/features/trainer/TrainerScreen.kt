package com.example.pokeapi.features.trainer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokeapi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("TRAINER CARD", fontWeight = FontWeight.Black, color = Color.White) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF3B7CBE))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F3F6))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F3F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Aquí iría el avatar del entrenador
                        Icon(
                            imageVector = Icons.Default.CatchingPokemon,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFF3B7CBE)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "ASH KETCHUM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "PALLET TOWN",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TrainerStat(icon = Icons.Default.CatchingPokemon, label = "Pokemons", value = "151")
                        TrainerStat(icon = Icons.Default.Badge, label = "Badges", value = "8")
                        TrainerStat(icon = Icons.Default.Star, label = "Rank", value = "Master")
                    }
                }
            }
        }
    }
}

@Composable
fun TrainerStat(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color(0xFF3B7CBE), modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}
