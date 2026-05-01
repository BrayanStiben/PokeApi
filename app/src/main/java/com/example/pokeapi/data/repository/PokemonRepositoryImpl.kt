package com.example.pokeapi.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.pokeapi.data.PokeApi
import com.example.pokeapi.data.PokeDatabase
import com.example.pokeapi.data.model.PokemonEntity
import com.example.pokeapi.data.model.PokemonStatEntity
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.domain.model.PokemonStat
import com.example.pokeapi.domain.repository.PokemonRepository
import com.example.pokeapi.data.mapper.toPokemon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApi,
    private val db: PokeDatabase
) : PokemonRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemons(query: String, type: String?): Flow<PagingData<Pokemon>> {
        val pagingSourceFactory = {
            db.pokemonDao().searchPokemons(query, type)
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = PokemonRemoteMediator(api, db, query, type),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                Pokemon(
                    id = entity.id,
                    name = entity.name,
                    imageUrl = entity.imageUrl,
                    types = if (entity.types.isNotBlank()) entity.types.split(",") else emptyList()
                )
            }
        }
    }

    override suspend fun getPokemonDetail(id: Int): Result<Pokemon> {
        return try {
            val localEntity = db.pokemonDao().getPokemonById(id)
            if (localEntity != null && localEntity.description.isNotBlank()) {
                val stats = db.pokemonDao().getStatsForPokemon(id).map {
                    PokemonStat(it.name, it.value)
                }
                Result.success(
                    Pokemon(
                        id = localEntity.id,
                        name = localEntity.name,
                        imageUrl = localEntity.imageUrl,
                        types = localEntity.types.split(","),
                        height = localEntity.height,
                        weight = localEntity.weight,
                        abilities = localEntity.abilities.split(","),
                        stats = stats,
                        description = localEntity.description
                    )
                )
            } else {
                // Sincronizado con PokeApi.kt
                val detailDto = api.getPokemonDetail(id.toString())
                val speciesDto = api.getPokemonSpecies(id)
                val description = speciesDto.flavorTextEntries
                    .find { it.language.name == "en" }
                    ?.flavorText?.replace("\n", " ") ?: ""
                
                val pokemon = detailDto.toPokemon(description)
                
                db.withTransaction {
                    val entity = PokemonEntity(
                        id = pokemon.id,
                        name = pokemon.name,
                        imageUrl = pokemon.imageUrl,
                        types = pokemon.types.joinToString(","),
                        height = pokemon.height,
                        weight = pokemon.weight,
                        abilities = pokemon.abilities.joinToString(","),
                        description = pokemon.description
                    )
                    db.pokemonDao().insertAll(listOf(entity))
                    
                    val statsEntities = pokemon.stats.map {
                        PokemonStatEntity(
                            pokemonId = pokemon.id,
                            name = it.name,
                            value = it.value
                        )
                    }
                    db.pokemonDao().insertStats(statsEntities)
                }
                
                Result.success(pokemon)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTypes(): Result<List<String>> {
        return try {
            val response = api.getTypes()
            Result.success(response.results.map { it.name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
