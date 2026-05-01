package com.example.pokeapi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// --- ENTITIES (Room) ---

@Entity(tableName = "pokemon_table")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val types: String,
    val height: Int,
    val weight: Int,
    val abilities: String,
    val description: String
)

@Entity(tableName = "pokemon_stat_table")
data class PokemonStatEntity(
    @PrimaryKey(autoGenerate = true) val statId: Int = 0,
    val pokemonId: Int,
    val name: String,
    val value: Int
)

@Entity(tableName = "remote_keys")
data class RemoteKeysEntity(
    @PrimaryKey val pokemonId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)

// --- DTOs (Retrofit) ---

data class PokemonListResponse(val results: List<PokemonSimpleDto>)
data class PokemonSimpleDto(val name: String, val url: String)

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
data class OtherSpritesDto(@SerializedName("official-artwork") val officialArtwork: OfficialArtworkDto?)
data class OfficialArtworkDto(@SerializedName("front_default") val frontDefault: String?)
data class TypeSlotDto(val type: TypeDto)
data class TypeDto(val name: String)
data class AbilitySlotDto(val ability: AbilityDto)
data class AbilityDto(val name: String)
data class StatSlotDto(@SerializedName("base_stat") val baseStat: Int, val stat: StatDto)
data class StatDto(val name: String)

data class PokemonSpeciesDto(@SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto>)
data class FlavorTextEntryDto(@SerializedName("flavor_text") val flavorText: String, val language: LanguageDto)
data class LanguageDto(val name: String)

data class TypeListResponse(val results: List<TypeItemDto>)
data class TypeItemDto(val name: String, val url: String)

data class TypeDetailResponse(
    val pokemon: List<TypePokemonSlotDto>
)

data class TypePokemonSlotDto(
    val pokemon: PokemonSimpleDto
)
