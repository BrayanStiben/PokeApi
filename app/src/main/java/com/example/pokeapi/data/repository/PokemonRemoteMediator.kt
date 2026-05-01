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
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 0
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val pokemonEntities: List<PokemonEntity>
            val endOfPaginationReached: Boolean

            if (type != null && loadType == LoadType.REFRESH) {
                // Special case: Filter by type
                val typeResponse = api.getPokemonByType(type)
                pokemonEntities = typeResponse.pokemon.map { slot ->
                    val id = slot.pokemon.url.split("/").filter { it.isNotEmpty() }.last().toInt()
                    PokemonEntity(
                        id = id,
                        name = slot.pokemon.name.replaceFirstChar { it.uppercase() },
                        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                        types = type, // Fill the type so the local filter finds it
                        height = 0,
                        weight = 0,
                        abilities = "",
                        description = ""
                    )
                }
                endOfPaginationReached = true // We got all for this type in one go
            } else if (type != null) {
                // If we already loaded a type list, there's no more paging for now
                return MediatorResult.Success(endOfPaginationReached = true)
            } else {
                // Normal sequential paging (or searching in a larger set)
                // If it's a REFRESH and we have a query, we might want a bigger page to find results
                val limit = if (loadType == LoadType.REFRESH && query.isNotBlank()) 100 else state.config.pageSize
                val offset = page * state.config.pageSize
                
                val apiResponse = api.getPokemonList(limit, offset)
                pokemonEntities = apiResponse.results.map { simpleDto ->
                    val id = simpleDto.url.split("/").filter { it.isNotEmpty() }.last().toInt()
                    PokemonEntity(
                        id = id,
                        name = simpleDto.name.replaceFirstChar { it.uppercase() },
                        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png",
                        types = "",
                        height = 0,
                        weight = 0,
                        abilities = "",
                        description = ""
                    )
                }
                endOfPaginationReached = apiResponse.results.isEmpty()
            }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.remoteKeysDao().clearRemoteKeys()
                    db.pokemonDao().clearAll()
                }
                
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

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PokemonEntity>): RemoteKeysEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { pokemon ->
                db.remoteKeysDao().remoteKeysPokemonId(pokemon.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, PokemonEntity>): RemoteKeysEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { pokemon ->
                db.remoteKeysDao().remoteKeysPokemonId(pokemon.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, PokemonEntity>
    ): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                db.remoteKeysDao().remoteKeysPokemonId(id)
            }
        }
    }
}
