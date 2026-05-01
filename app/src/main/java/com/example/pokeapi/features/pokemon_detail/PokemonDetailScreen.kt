package com.example.pokeapi.features.pokemon_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.domain.model.PokemonStat
import com.example.pokeapi.features.pokemon_list.getPokemonTypeColor

@Composable
fun PokemonDetailScreen(
    onBackClick: () -> Unit,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Explore, null) }, label = { Text("Explorer") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Favorite, null) }, label = { Text("Favorites") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Trainer") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.pokemon != null) {
                DetailContent(pokemon = state.pokemon!!, onBackClick = onBackClick)
            }
        }
    }
}

@Composable
fun DetailContent(pokemon: Pokemon, onBackClick: () -> Unit) {
    val primaryColor = getPokemonTypeColor(pokemon.types.firstOrNull() ?: "")
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header con degradado y nombre
        Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(Brush.verticalGradient(listOf(primaryColor, primaryColor.copy(0.6f))))) {
            Column(modifier = Modifier.padding(top = 40.dp, start = 20.dp, end = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    IconButton(onClick = {}) { Icon(Icons.Default.FavoriteBorder, null, tint = Color.White) }
                }
                Text(pokemon.name.uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("#${pokemon.id.toString().padStart(3, '0')}", color = Color.White.copy(0.8f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(200.dp).align(Alignment.BottomCenter).offset(y = 40.dp)
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // ABOUT
            SectionHeader("ABOUT")
            InfoCard {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailInfoItem(Icons.Default.LocalFireDepartment, "Type", pokemon.types.joinToString(" / "), Modifier.weight(1f))
                    DetailInfoItem(Icons.Default.Height, "Height", "${pokemon.height / 10.0} m", Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailInfoItem(Icons.Default.MonitorWeight, "Weight", "${pokemon.weight / 10.0} kg", Modifier.weight(1f))
                    DetailInfoItem(Icons.Default.Face, "Abilities", pokemon.abilities.take(2).joinToString(", "), Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // STATS
            SectionHeader("BASE STATS")
            InfoCard {
                pokemon.stats.forEach { stat ->
                    StatRow(stat, primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MOVES (TOP 3)
            SectionHeader("MOVES (TOP 3)")
            pokemon.moves.take(3).forEach { move ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.OfflineBolt, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(move.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

@Composable
fun DetailInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatRow(stat: PokemonStat, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(stat.name.uppercase(), modifier = Modifier.width(70.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        LinearProgressIndicator(
            progress = { stat.value / 150f },
            modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(0.1f)
        )
        Text(stat.value.toString(), modifier = Modifier.width(35.dp), textAlign = TextAlign.End, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
