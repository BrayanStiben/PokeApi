package com.example.pokeapi.domain.model

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String> = emptyList(),
    val height: Int = 0,
    val weight: Int = 0,
    val abilities: List<String> = emptyList(),
    val stats: List<PokemonStat> = emptyList(),
    val description: String = "",
    val moves: List<String> = emptyList(),
    val evolutionChain: List<EvolutionStep> = emptyList(),
    val isFavorite: Boolean = false // Nuevo campo
)

data class PokemonStat(
    val name: String,
    val value: Int
)

data class EvolutionStep(
    val id: Int,
    val name: String,
    val imageUrl: String
)
