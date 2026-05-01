package com.example.pokeapi.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.pokeapi.data.PokeApi
import com.example.pokeapi.data.PokeDatabase
import com.example.pokeapi.data.model.*
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.domain.model.PokemonMove
import com.example.pokeapi.domain.model.PokemonStat
import com.example.pokeapi.domain.repository.PokemonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApi,
    private val db: PokeDatabase
) : PokemonRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemons(query: String, type: String?, category: String?): Flow<PagingData<Pokemon>> {
        val pagingSourceFactory = {
            db.pokemonDao().searchPokemons(query, type, category)
        }

        return Pager(
            config = PagingConfig(
                pageSize = 40,
                prefetchDistance = 40, // Empezar a cargar antes
                initialLoadSize = 80, // Carga inicial más grande
                enablePlaceholders = true // RESERVAR ESPACIO PARA EVITAR SALTOS
            ),
            remoteMediator = PokemonRemoteMediator(api, db, query, type, category),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                Pokemon(
                    id = entity.id,
                    name = entity.name,
                    imageUrl = entity.imageUrl,
                    types = if (entity.types.isNotBlank()) entity.types.split(",") else emptyList(),
                    isFavorite = entity.isFavorite
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
                val moves = db.pokemonDao().getMovesForPokemon(id).map {
                    PokemonMove(it.name, it.type, it.damageClass, it.learnMethod, it.level)
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
                        description = localEntity.description,
                        moves = moves,
                        isFavorite = localEntity.isFavorite
                    )
                )
            } else {
                val detailDto = api.getPokemonDetail(id.toString())
                
                val speciesName = detailDto.species.name
                val description = try {
                    val speciesDto = api.getPokemonSpecies(speciesName.lowercase())
                    speciesDto.flavorTextEntries
                        .find { it.language.name == "en" }
                        ?.flavorText?.replace("\n", " ") ?: ""
                } catch (e: Exception) { "" }

                // Fetch Move Details with Cache
                val detailedMoves = coroutineScope {
                    detailDto.moves.map { moveSlot ->
                        async {
                            try {
                                val moveName = moveSlot.move.name
                                var moveInfo = db.pokemonDao().getMoveDetail(moveName)
                                
                                if (moveInfo == null) {
                                    val moveDetail = api.getMoveDetail(moveName)
                                    moveInfo = MoveDetailEntity(
                                        name = moveName,
                                        type = moveDetail.type.name,
                                        damageClass = moveDetail.damageClass?.name ?: "status"
                                    )
                                    db.pokemonDao().insertMoveDetail(moveInfo)
                                }
                                
                                val learnInfo = moveSlot.versionGroupDetails.firstOrNull()
                                PokemonMove(
                                    name = moveName.replace("-", " "),
                                    type = moveInfo.type,
                                    damageClass = moveInfo.damageClass,
                                    learnMethod = learnInfo?.moveLearnMethod?.name ?: "unknown",
                                    level = learnInfo?.levelLearnedAt ?: 0
                                )
                            } catch (e: Exception) { null }
                        }
                    }.awaitAll().filterNotNull()
                }

                val pokemon = Pokemon(
                    id = detailDto.id,
                    name = detailDto.name.replaceFirstChar { it.uppercase() },
                    imageUrl = detailDto.sprites.other?.officialArtwork?.frontDefault ?: detailDto.sprites.frontDefault ?: "",
                    types = detailDto.types.map { it.type.name },
                    height = detailDto.height,
                    weight = detailDto.weight,
                    abilities = detailDto.abilities.map { it.ability.name },
                    stats = detailDto.stats.map { PokemonStat(it.stat.name, it.baseStat) },
                    description = description,
                    moves = detailedMoves,
                    isFavorite = localEntity?.isFavorite ?: false
                )

                db.withTransaction {
                    val entity = PokemonEntity(
                        id = pokemon.id,
                        name = pokemon.name,
                        imageUrl = pokemon.imageUrl,
                        types = pokemon.types.joinToString(","),
                        height = pokemon.height,
                        weight = pokemon.weight,
                        abilities = pokemon.abilities.joinToString(","),
                        description = pokemon.description,
                        moves = "",
                        isFavorite = pokemon.isFavorite
                    )
                    db.pokemonDao().insertAll(listOf(entity))
                    
                    db.pokemonDao().insertStats(pokemon.stats.map {
                        PokemonStatEntity(pokemonId = pokemon.id, name = it.name, value = it.value)
                    })

                    db.pokemonDao().deleteMovesForPokemon(pokemon.id)
                    db.pokemonDao().insertMoves(detailedMoves.map {
                        PokemonMoveEntity(
                            pokemonId = pokemon.id,
                            name = it.name,
                            type = it.type,
                            damageClass = it.damageClass,
                            learnMethod = it.learnMethod,
                            level = it.level
                        )
                    })
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
            val filteredTypes = response.results
                .map { it.name }
                .filter { it != "stellar" && it != "unknown" }
            Result.success(filteredTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavoritePokemons(): Flow<List<Pokemon>> {
        return db.pokemonDao().getFavoritePokemons().map { entities ->
            entities.map { entity ->
                Pokemon(
                    id = entity.id,
                    name = entity.name,
                    imageUrl = entity.imageUrl,
                    types = entity.types.split(",").filter { it.isNotBlank() },
                    isFavorite = true
                )
            }
        }
    }

    override suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        db.pokemonDao().updateFavoriteStatus(id, isFavorite)
    }
}
