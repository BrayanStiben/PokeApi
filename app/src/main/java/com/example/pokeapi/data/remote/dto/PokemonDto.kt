package com.example.pokeapi.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PokemonListResponse(
    val results: List<PokemonSimpleDto>
)

data class PokemonSimpleDto(
    val name: String,
    val url: String
)

data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: SpritesDto,
    val types: List<TypeSlotDto>,
    val abilities: List<AbilitySlotDto>,
    val stats: List<StatSlotDto>
)

data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?,
    @SerializedName("other") val other: OtherSpritesDto?
)

data class OtherSpritesDto(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtworkDto?
)

data class OfficialArtworkDto(
    @SerializedName("front_default") val frontDefault: String?
)

data class TypeSlotDto(
    val type: TypeDto
)

data class TypeDto(
    val name: String
)

data class AbilitySlotDto(
    val ability: AbilityDto
)

data class AbilityDto(
    val name: String
)

data class StatSlotDto(
    @SerializedName("base_stat") val baseStat: Int,
    val stat: StatDto
)

data class StatDto(
    val name: String
)

data class PokemonSpeciesDto(
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto>
)

data class FlavorTextEntryDto(
    @SerializedName("flavor_text") val flavorText: String,
    val language: LanguageDto
)

data class LanguageDto(
    val name: String
)
