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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val api: PokeApi,
    private val db: PokeDatabase,
    private val query: String = "",
    private val type: String? = null,
    private val category: String? = null
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        // ¿Estamos en modo búsqueda/filtro?
        val isSearchMode = query.isNotBlank() || type != null || category != null

        val offset = when (loadType) {
            LoadType.REFRESH -> {
                val anchorPosition = state.anchorPosition
                val item = anchorPosition?.let { state.closestItemToPosition(it) }
                
                if (item != null) {
                    // Calculamos la página real basada en el ID del pokemon que vemos
                    ((item.id - 1) / state.config.pageSize) * state.config.pageSize
                } else if (query.isNotBlank()) {
                    val numericId = query.trim().toIntOrNull()
                    if (numericId != null) ((numericId - 1) / state.config.pageSize).coerceAtLeast(0) * state.config.pageSize else 0
                } else {
                    0
                }
            }
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                if (isSearchMode && type == null && category == null) {
                    // En búsqueda simple por nombre/id, no paginamos más allá del resultado
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                
                // Buscar la última llave de la secuencia normal
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                
                if (nextKey == null) {
                    // Si no hay llave, usamos el ID del último item de la secuencia
                    val lastItem = state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
                    if (lastItem != null) lastItem.id else 0
                } else {
                    nextKey
                }
            }
        }.coerceAtLeast(0)

        try {
            val pokemonEntitiesFromNetwork = mutableListOf<PokemonEntity>()
            var endOfPaginationReached = false

            when {
                // Filtros de Tipo/Categoría (Traen todo de una vez)
                (type != null || category != null) && loadType == LoadType.REFRESH -> {
                    val names = if (type != null) {
                        api.getPokemonByType(type).pokemon.map { it.pokemon.name }
                    } else {
                        val apiResponse = api.getPokemonList(limit = 2000, offset = 0)
                        val searchTerm = "-${category?.lowercase()}"
                        apiResponse.results.map { it.name }.filter { name -> name.contains(searchTerm) }
                    }
                    pokemonEntitiesFromNetwork.addAll(fetchDetailsInParallel(names))
                    endOfPaginationReached = true
                }

                // Carga Secuencial Normal (O búsqueda por ID que se integra en la secuencia)
                else -> {
                    val limit = state.config.pageSize
                    val apiResponse = api.getPokemonList(limit = limit, offset = offset)
                    val items = apiResponse.results
                    pokemonEntitiesFromNetwork.addAll(fetchDetailsInParallel(items.map { it.name }))
                    endOfPaginationReached = items.isEmpty() || items.size < limit
                }
            }

            db.withTransaction {
                // YA NO BORRAMOS NADA. Solo insertamos o actualizamos.
                
                val favoriteIds = db.pokemonDao().getFavoriteIds().toSet()
                val finalEntities = pokemonEntitiesFromNetwork.map { entity ->
                    if (favoriteIds.contains(entity.id)) entity.copy(isFavorite = true) else entity
                }
                db.pokemonDao().insertAll(finalEntities)

                // IMPORTANTE: Solo guardamos llaves de navegación si NO hay filtros activos.
                // Esto mantiene la "columna vertebral" del 1 al 1000 intacta.
                if (!isSearchMode) {
                    val prevKey = if (offset == 0) null else offset - state.config.pageSize
                    val nextKey = if (endOfPaginationReached) null else offset + state.config.pageSize

                    val keys = finalEntities.map {
                        RemoteKeysEntity(pokemonId = it.id, prevKey = prevKey, nextKey = nextKey)
                    }
                    db.remoteKeysDao().insertAll(keys)
                }
            }
            
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun fetchDetailsInParallel(names: List<String>): List<PokemonEntity> = coroutineScope {
        names.chunked(5).flatMap { chunk ->
            chunk.map { name ->
                async {
                    try {
                        val detail = api.getPokemonDetail(name)
                        mapDetailToEntity(detail)
                    } catch (e: Exception) { null }
                }
            }.awaitAll().filterNotNull()
        }
    }

    private fun mapDetailToEntity(detail: com.example.pokeapi.data.model.PokemonDetailDto): PokemonEntity {
        return PokemonEntity(
            id = detail.id,
            name = detail.name.replaceFirstChar { it.uppercase() },
            imageUrl = detail.sprites.other?.officialArtwork?.frontDefault 
                ?: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${detail.id}.png",
            types = detail.types.joinToString(",") { it.type.name },
            height = detail.height,
            weight = detail.weight,
            abilities = detail.abilities.joinToString(",") { it.ability.name },
            description = ""
        )
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PokemonEntity>): RemoteKeysEntity? {
        // Buscamos la última llave válida en la secuencia principal
        return state.pages.flatMap { it.data }.reversed().firstNotNullOfOrNull { pokemon ->
            db.remoteKeysDao().remoteKeysPokemonId(pokemon.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, PokemonEntity>): RemoteKeysEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                db.remoteKeysDao().remoteKeysPokemonId(id)
            }
        }
    }
}
