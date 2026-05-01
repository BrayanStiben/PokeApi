package com.example.pokeapi.features.pokemon_list

import com.example.pokeapi.core.utils.ConnectivityObserver

data class PokemonListState(
    val searchQuery: String = "",
    val networkStatus: ConnectivityObserver.Status = ConnectivityObserver.Status.Unavailable,
    val types: List<String> = listOf("normal", "fire", "water", "grass", "electric", "ice", "fighting", "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "steel", "fairy"),
    val selectedType: String? = null,
    val categories: List<String> = listOf("MEGA", "GMAX", "ALOLA", "GALAR", "HISUI", "PALDEA"),
    val selectedCategory: String? = null,
    val isTypeDropdownExpanded: Boolean = false
)

sealed interface PokemonListEvent {
    data class OnSearchQueryChange(val query: String) : PokemonListEvent
    data class OnTypeSelected(val type: String?) : PokemonListEvent
    data class OnCategorySelected(val category: String?) : PokemonListEvent
    object OnToggleTypeDropdown : PokemonListEvent
    data class OnToggleFavorite(val pokemonId: Int, val isFavorite: Boolean) : PokemonListEvent
}
