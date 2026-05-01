package com.example.pokeapi.data.mapper

import com.example.pokeapi.data.model.PokemonDetailDto
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.domain.model.PokemonStat

fun PokemonDetailDto.toPokemon(description: String = ""): Pokemon {
    return Pokemon(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = sprites.other?.officialArtwork?.frontDefault ?: sprites.frontDefault ?: "",
        types = types.map { it.type.name },
        height = height,
        weight = weight,
        abilities = abilities.map { it.ability.name },
        stats = stats.map { PokemonStat(it.stat.name, it.baseStat) },
        description = description
    )
}
