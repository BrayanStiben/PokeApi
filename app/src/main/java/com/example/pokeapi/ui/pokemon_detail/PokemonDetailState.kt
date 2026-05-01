package com.example.pokeapi.ui.pokemon_detail

import com.example.pokeapi.domain.model.Pokemon

data class PokemonDetailState(
    val pokemon: Pokemon? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface PokemonDetailEvent {
    object OnRetry : PokemonDetailEvent
    object OnBackClick : PokemonDetailEvent
}
