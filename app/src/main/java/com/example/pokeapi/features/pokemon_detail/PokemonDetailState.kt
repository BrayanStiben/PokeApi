package com.example.pokeapi.features.pokemon_detail

import com.example.pokeapi.domain.model.Pokemon

data class PokemonDetailState(
    val pokemon: Pokemon? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isConnectionError: Boolean = false,
    val pokemonName: String = ""
)

sealed interface PokemonDetailEvent {
    object OnRetry : PokemonDetailEvent
    object OnBackClick : PokemonDetailEvent
    data class OnToggleFavorite(val pokemonId: Int, val isFavorite: Boolean) : PokemonDetailEvent
}
