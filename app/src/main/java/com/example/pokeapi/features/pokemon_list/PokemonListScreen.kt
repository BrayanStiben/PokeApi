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
import coil.compose.AsyncImage
import com.example.pokeapi.domain.model.Pokemon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    onTrainerClick: () -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pokemons = viewModel.pokemons.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("POKÉDEX EXPLORER", fontWeight = FontWeight.Black, color = Color.White) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF3B7CBE)),
                actions = {
                    IconButton(onClick = onTrainerClick) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Explore, null) },
                    label = { Text("Explorer") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onFavoritesClick,
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onTrainerClick,
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Trainer") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }, containerColor = Color(0xFF3B7CBE), shape = CircleShape) {
                Icon(Icons.Default.CatchingPokemon, null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F3F6))
        ) {
            // Buscador y Filtros
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SEARCH BY NAME OR ID", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onEvent(PokemonListEvent.OnSearchQueryChange(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Pikachu...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("FILTER BY TYPE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                        TextButton(onClick = { viewModel.onEvent(PokemonListEvent.OnTypeSelected(null)) }) {
                            Text("RESET FILTERS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.types) { type ->
                            FilterChip(
                                selected = state.selectedType == type,
                                onClick = { 
                                    viewModel.onEvent(PokemonListEvent.OnTypeSelected(if (state.selectedType == type) null else type)) 
                                },
                                label = { Text(type.replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getPokemonTypeColor(type),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                border = null
                            )
                        }
                    }
                }
            }

            // Grid de Pokemon
            Box(modifier = Modifier.fillMaxSize()) {
                if (pokemons.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (pokemons.itemCount == 0) {
                    Text("No results found", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pokemons.itemCount) { index ->
                        pokemons[index]?.let { pokemon ->
                            PokemonCard(pokemon = pokemon, onClick = { onPokemonClick(pokemon.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonCard(pokemon: Pokemon, onClick: () -> Unit) {
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
                Icon(
                    Icons.Default.FavoriteBorder, null,
                    modifier = Modifier.size(16.dp).align(Alignment.TopEnd),
                    tint = Color.Red.copy(alpha = 0.3f)
                )
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
