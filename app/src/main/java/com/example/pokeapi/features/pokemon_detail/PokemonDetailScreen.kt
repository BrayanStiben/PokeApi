package com.example.pokeapi.features.pokemon_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.domain.model.PokemonMove
import com.example.pokeapi.domain.model.PokemonStat
import com.example.pokeapi.features.pokemon_list.getPokemonTypeColor
import com.example.pokeapi.features.sinwifi.NoWifiScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PokemonDetailScreen(
    onBackClick: () -> Unit,
    viewModel: PokemonDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.isConnectionError && state.pokemon == null) {
                NoWifiScreen(
                    pokemonName = state.pokemonName,
                    onRetry = { viewModel.onEvent(PokemonDetailEvent.OnRetry) },
                    onBack = onBackClick
                )
            } else if (state.pokemon != null) {
                DetailContent(
                    pokemon = state.pokemon!!,
                    onBackClick = onBackClick,
                    onFavoriteClick = { id, isFavorite ->
                        viewModel.onEvent(PokemonDetailEvent.OnToggleFavorite(id, isFavorite))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailContent(
    pokemon: Pokemon,
    onBackClick: () -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit
) {
    val primaryColor = getPokemonTypeColor(pokemon.types.firstOrNull() ?: "")
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(Brush.verticalGradient(listOf(primaryColor, primaryColor.copy(0.6f))))) {
            Column(modifier = Modifier.padding(top = 40.dp, start = 20.dp, end = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                    IconButton(onClick = { onFavoriteClick(pokemon.id, !pokemon.isFavorite) }) {
                        Icon(
                            if (pokemon.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null,
                            tint = Color.White
                        )
                    }
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
            BaseStatsTable(pokemon.stats)

            Spacer(modifier = Modifier.height(24.dp))

            // MOVES
            SectionHeader("MOVES BY CATEGORY")
            
            val groupedMoves = pokemon.moves.groupBy { it.learnMethod }
            
            groupedMoves.forEach { (method, moves) ->
                CollapsibleMoveSection(method, moves, primaryColor)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BaseStatsTable(stats: List<PokemonStat>) {
    val totalStats = stats.sumOf { it.value }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Características base",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Rows
            stats.forEach { stat ->
                StatRow(stat)
                Divider(color = Color.Black.copy(0.1f), thickness = 0.5.dp)
            }

            // Total Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        totalStats.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.weight(2f))
            }
        }
    }
}

@Composable
fun StatRow(stat: PokemonStat) {
    val (statName, icon, iconColor, barColor) = when (stat.name.lowercase()) {
        "hp" -> Quad("PS", Icons.Default.Favorite, Color(0xFF4CAF50), Color(0xFF8BC34A))
        "attack" -> Quad("Ataque", Icons.Default.Bolt, Color(0xFFFF9800), Color(0xFF4DD0E1))
        "defense" -> Quad("Defensa", Icons.Default.Shield, Color(0xFFF44336), Color(0xFFFFF176))
        "special-attack" -> Quad("At. Esp.", Icons.Default.AutoAwesome, Color(0xFF03A9F4), Color(0xFFFFB74D))
        "special-defense" -> Quad("Def. Esp.", Icons.Default.VerifiedUser, Color(0xFF3F51B5), Color(0xFFFFF176))
        "speed" -> Quad("Velocidad", Icons.Default.Speed, Color(0xFF9C27B0), Color(0xFFFFF176))
        else -> Quad(stat.name.uppercase(), Icons.Default.Info, Color.Gray, Color.Gray)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon + Name
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF444444))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                statName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Value
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stat.value.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Bar
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(stat.value / 255f)
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(barColor)
                    .border(0.5.dp, Color.Black.copy(0.3f), CircleShape)
            )
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollapsibleMoveSection(method: String, moves: List<PokemonMove>, primaryColor: Color) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    
    val methodName = when(method.lowercase()) {
        "level-up" -> "BY LEVEL"
        "machine" -> "BY TM/HM"
        "tutor" -> "BY TUTOR"
        "egg" -> "BY EGG"
        else -> "OTHERS (${method.uppercase()})"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                methodName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = primaryColor
            )
            Icon(
                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = primaryColor
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moves.sortedBy { it.level }.forEach { move ->
                    MoveItem(move)
                }
            }
        }
        Divider(color = Color.LightGray.copy(0.5f))
    }
}

@Composable
fun MoveItem(move: PokemonMove) {
    val typeColor = getPokemonTypeColor(move.type)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(typeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    move.name.uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                if (move.level > 0) {
                    Text(
                        " Lvl ${move.level}",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tipo
                Surface(
                    color = typeColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        move.type.uppercase(),
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                // Clase de daño
                val classIcon = when(move.damageClass.lowercase()) {
                    "physical" -> Icons.Default.Gavel
                    "special" -> Icons.Default.AutoAwesome
                    else -> Icons.Default.Info
                }
                Icon(
                    classIcon,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.Gray
                )
                Text(
                    move.damageClass.uppercase(),
                    fontSize = 7.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
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
