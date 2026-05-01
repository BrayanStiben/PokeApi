package com.example.pokeapi.features.pokemon_list

import com.example.pokeapi.core.utils.ConnectivityObserver

data class PokemonListState(
    val searchQuery: String = "",
    val networkStatus: ConnectivityObserver.Status = ConnectivityObserver.Status.Unavailable,
    val types: List<String> = emptyList(),
    val selectedType: String? = null,
    val isTypeDropdownExpanded: Boolean = false
)

sealed interface PokemonListEvent {
    data class OnSearchQueryChange(val query: String) : PokemonListEvent
    data class OnTypeSelected(val type: String?) : PokemonListEvent
    object OnToggleTypeDropdown : PokemonListEvent
}
