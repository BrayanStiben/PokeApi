package com.example.pokeapi.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.pokeapi.data.PokeApi
import com.example.pokeapi.data.PokeDatabase
import com.example.pokeapi.data.model.PokemonEntity
import com.example.pokeapi.data.model.RemoteKeysEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val api: PokeApi,
    private val db: PokeDatabase,
    private val query: String = "",
    private val type: String? = null
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val pokemonEntities = mutableListOf<PokemonEntity>()
            var endOfPaginationReached = false

            // IMPORTANTE: Si es REFRESH, limpiamos la DB para el nuevo filtro/búsqueda
            if (loadType == LoadType.REFRESH) {
                db.withTransaction {
                    db.remoteKeysDao().clearRemoteKeys()
                    db.pokemonDao().clearAll()
                }

                if (type != null) {
                    // Carga por TIPO (la API devuelve todos los de ese tipo)
                    val typeResponse = api.getPokemonByType(type)
                    pokemonEntities.addAll(typeResponse.pokemon.map { slot ->
                        val id = slot.pokemon.url.split("/").filter { it.isNotEmpty() }.last().toInt()
                        PokemonEntity(
                            id = id,
                            name = slot.pokemon.name.replaceFirstChar { it.uppercase() },
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                            types = type, // Rellenamos el tipo para que el DAO lo encuentre
                            height = 0, weight = 0, abilities = "", description = ""
                        )
                    })
                    endOfPaginationReached = true 
                } else if (query.isNotBlank()) {
                    // Búsqueda específica
                    try {
                        val detail = api.getPokemonDetail(query.lowercase().trim())
                        pokemonEntities.add(PokemonEntity(
                            id = detail.id,
                            name = detail.name.replaceFirstChar { it.uppercase() },
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${detail.id}.png",
                            types = detail.types.joinToString(",") { it.type.name },
                            height = detail.height, weight = detail.weight, abilities = "", description = ""
                        ))
                        endOfPaginationReached = true
                    } catch (e: Exception) {
                        // Si falla la búsqueda exacta, cargamos los primeros 100
                        val apiResponse = api.getPokemonList(100, 0)
                        pokemonEntities.addAll(apiResponse.results.map { mapToEntity(it.name, it.url) })
                        endOfPaginationReached = false
                    }
                } else {
                    // Carga normal
                    val apiResponse = api.getPokemonList(state.config.pageSize, 0)
                    pokemonEntities.addAll(apiResponse.results.map { mapToEntity(it.name, it.url) })
                    endOfPaginationReached = apiResponse.results.isEmpty()
                }
            } else {
                // APPEND secuencial
                if (type != null) return MediatorResult.Success(endOfPaginationReached = true)
                
                val apiResponse = api.getPokemonList(state.config.pageSize, page * state.config.pageSize)
                pokemonEntities.addAll(apiResponse.results.map { mapToEntity(it.name, it.url) })
                endOfPaginationReached = apiResponse.results.isEmpty()
            }

            db.withTransaction {
                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                
                val keys = pokemonEntities.map {
                    RemoteKeysEntity(pokemonId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                db.remoteKeysDao().insertAll(keys)
                db.pokemonDao().insertAll(pokemonEntities)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private fun mapToEntity(name: String, url: String): PokemonEntity {
        val id = url.split("/").filter { it.isNotEmpty() }.last().toInt()
        return PokemonEntity(
            id = id,
            name = name.replaceFirstChar { it.uppercase() },
            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
            types = "", 
            height = 0, weight = 0, abilities = "", description = ""
        )
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PokemonEntity>): RemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { pokemon -> db.remoteKeysDao().remoteKeysPokemonId(pokemon.id) }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, PokemonEntity>): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                db.remoteKeysDao().remoteKeysPokemonId(id)
            }
        }
    }
}
