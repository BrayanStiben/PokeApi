package com.example.pokeapi.features.pokemon_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.core.utils.ConnectivityObserver

@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pokemons = viewModel.pokemons.collectAsLazyPagingItems()

    PokemonListContent(
        state = state,
        pokemons = pokemons,
        onEvent = viewModel::onEvent,
        onPokemonClick = onPokemonClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListContent(
    state: PokemonListState,
    pokemons: androidx.paging.compose.LazyPagingItems<Pokemon>,
    onEvent: (PokemonListEvent) -> Unit,
    onPokemonClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PokeApi Explorer") },
                actions = {
                    val statusText = when (state.networkStatus) {
                        ConnectivityObserver.Status.Available -> "Online"
                        else -> "Offline"
                    }
                    val statusColor = when (state.networkStatus) {
                        ConnectivityObserver.Status.Available -> Color.Green
                        else -> Color.Red
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(PokemonListEvent.OnSearchQueryChange(it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search Name/ID...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box {
                    IconButton(onClick = { onEvent(PokemonListEvent.OnToggleTypeDropdown) }) {
                        Icon(
                            Icons.Default.FilterList, 
                            contentDescription = "Filter",
                            tint = if (state.selectedType != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    DropdownMenu(
                        expanded = state.isTypeDropdownExpanded,
                        onDismissRequest = { onEvent(PokemonListEvent.OnToggleTypeDropdown) }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Types") },
                            onClick = { onEvent(PokemonListEvent.OnTypeSelected(null)) }
                        )
                        state.types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                onClick = { onEvent(PokemonListEvent.OnTypeSelected(type)) }
                            )
                        }
                    }
                }
            }

            if (state.selectedType != null) {
                InputChip(
                    selected = true,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = { onEvent(PokemonListEvent.OnTypeSelected(null)) },
                    label = { Text("Type: ${state.selectedType.replaceFirstChar { it.uppercase() }}") },
                    trailingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Close, 
                            contentDescription = "Clear filter", 
                            modifier = Modifier.size(18.dp)
                        ) 
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = pokemons.itemCount,
                    ) { index ->
                        val pokemon = pokemons[index]
                        if (pokemon != null) {
                            PokemonItem(
                                pokemon = pokemon,
                                onClick = { onPokemonClick(pokemon.id) }
                            )
                        }
                    }

                    item {
                        if (pokemons.loadState.append is LoadState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                if (pokemons.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                if (pokemons.loadState.refresh is LoadState.Error) {
                    val error = (pokemons.loadState.refresh as LoadState.Error).error
                    Text(
                        text = "Error: ${error.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PokemonItem(
    pokemon: Pokemon,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = pokemon.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
