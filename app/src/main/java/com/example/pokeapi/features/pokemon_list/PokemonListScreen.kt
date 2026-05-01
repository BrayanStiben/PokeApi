package com.example.pokeapi.features.pokemon_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.example.pokeapi.core.utils.ConnectivityObserver
import com.example.pokeapi.domain.model.Pokemon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onPokemonClick: (Int, String) -> Unit,
    onFavoritesClick: () -> Unit,
    onTrainerClick: () -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pokemons = viewModel.pokemons.collectAsLazyPagingItems()

    val pokeRed = Color(0xFFE3350D)
    val pokeWhite = Color.White
    val pokeGrey = Color(0xFFF5F5F5)

    // Reintento automático cuando vuelve el internet (solo si hay error)
    LaunchedEffect(state.networkStatus) {
        if (state.networkStatus == ConnectivityObserver.Status.Available && 
            (pokemons.loadState.refresh is LoadState.Error || pokemons.loadState.append is LoadState.Error)) {
            pokemons.retry()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = pokeRed,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "POKÉDEX",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = pokeWhite,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 2.dp)) {
                            val isOnline = state.networkStatus == ConnectivityObserver.Status.Available
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF51FF5F) else Color(0xFFFF5151))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isOnline) "SISTEMA ONLINE" else "SISTEMA OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = pokeWhite.copy(alpha = 0.9f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    
                    Surface(
                        onClick = onTrainerClick,
                        color = pokeWhite.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Entrenador",
                                tint = pokeWhite,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFavoritesClick,
                containerColor = pokeRed,
                contentColor = pokeWhite,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Favorite, null, modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(pokeGrey)
        ) {
            // Panel de Control
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = pokeWhite,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onEvent(PokemonListEvent.OnSearchQueryChange(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        placeholder = { Text("Buscar por nombre o ID...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = pokeRed) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onEvent(PokemonListEvent.OnSearchQueryChange("")) }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                                }
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = pokeGrey,
                            unfocusedContainerColor = pokeGrey,
                            focusedBorderColor = pokeRed.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                        ),
                        singleLine = true
                    )

                    Text(
                        "TIPOS POKÉMON",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 20.dp, bottom = 6.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.types) { type ->
                            val isSelected = state.selectedType == type
                            val typeColor = getPokemonTypeColor(type)
                            Surface(
                                onClick = { viewModel.onEvent(PokemonListEvent.OnTypeSelected(if (isSelected) null else type)) },
                                color = if (isSelected) typeColor else typeColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.4f)),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        type.uppercase(),
                                        color = if (isSelected) pokeWhite else typeColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 16.dp, bottom = 6.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FORMAS ESPECIALES", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
                        if (state.selectedType != null || state.selectedCategory != null) {
                            Text(
                                "LIMPIAR FILTROS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = pokeRed,
                                modifier = Modifier.clickable {
                                    viewModel.onEvent(PokemonListEvent.OnTypeSelected(null))
                                    viewModel.onEvent(PokemonListEvent.OnCategorySelected(null))
                                }
                            )
                        }
                    }
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.categories) { category ->
                            val isSelected = state.selectedCategory == category
                            Surface(
                                onClick = { viewModel.onEvent(PokemonListEvent.OnCategorySelected(if (isSelected) null else category)) },
                                color = if (isSelected) pokeRed else pokeWhite,
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = if (isSelected) 4.dp else 1.dp,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        category,
                                        color = if (isSelected) pokeWhite else Color.DarkGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Grid de Pokemon
            Box(modifier = Modifier.fillMaxSize()) {
                if (pokemons.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = pokeRed)
                } else if (pokemons.itemCount == 0) {
                    Text("No se encontraron resultados", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = pokemons.itemCount,
                        key = pokemons.itemKey { it.id },
                        contentType = pokemons.itemContentType { "pokemon" }
                    ) { index ->
                        val pokemon = pokemons[index]
                        if (pokemon != null) {
                            PokemonCard(
                                pokemon = pokemon,
                                onClick = { onPokemonClick(pokemon.id, pokemon.name) },
                                onFavoriteClick = { id, isFavorite ->
                                    viewModel.onEvent(PokemonListEvent.OnToggleFavorite(id, isFavorite))
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = pokeRed)
                            }
                        }
                    }

                    if (pokemons.loadState.append is LoadState.Error) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Error al cargar más", color = pokeRed, fontSize = 12.sp)
                                Button(
                                    onClick = { pokemons.retry() },
                                    colors = ButtonDefaults.buttonColors(containerColor = pokeRed)
                                ) {
                                    Text("Reintentar", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    
                    if (pokemons.loadState.append is LoadState.Loading) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = pokeRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonCard(
    pokemon: Pokemon,
    onClick: () -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "#${pokemon.id.toString().padStart(3, '0')}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                IconButton(
                    onClick = { onFavoriteClick(pokemon.id, !pokemon.isFavorite) },
                    modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
                ) {
                    Icon(
                        if (pokemon.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (pokemon.isFavorite) Color.Red else Color.Red.copy(alpha = 0.3f)
                    )
                }
            }
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                pokemon.name.uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pokemon.types.forEach { type ->
                    Surface(
                        color = getPokemonTypeColor(type),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            type.uppercase(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getPokemonTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "fire" -> Color(0xFFFF421C)
        "water" -> Color(0xFF5CBEFA)
        "grass" -> Color(0xFF5FBD58)
        "poison" -> Color(0xFF924593)
        "electric" -> Color(0xFFF2D94E)
        "psychic" -> Color(0xFFF65687)
        "ice" -> Color(0xFF51C4E7)
        "dragon" -> Color(0xFF5366D3)
        "fairy" -> Color(0xFFE29FE9)
        "bug" -> Color(0xFF92BC2C)
        "ground" -> Color(0xFFDA7C4D)
        "rock" -> Color(0xFFB9A156)
        "ghost" -> Color(0xFF705898)
        "steel" -> Color(0xFFB8B8D0)
        "fighting" -> Color(0xFFC03028)
        "normal" -> Color(0xFFA8A878)
        "flying" -> Color(0xFFA890F0)
        else -> Color(0xFF3B7CBE)
    }
}
