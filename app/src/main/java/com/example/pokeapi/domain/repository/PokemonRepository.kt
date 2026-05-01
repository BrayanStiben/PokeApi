package com.example.pokeapi.domain.repository

import androidx.paging.PagingData
import com.example.pokeapi.domain.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun getPokemons(query: String = "", type: String? = null, category: String? = null): Flow<PagingData<Pokemon>>
    suspend fun getPokemonDetail(id: Int): Result<Pokemon>
    suspend fun getTypes(): Result<List<String>>
    
    // Favoritos
    fun getFavoritePokemons(): Flow<List<Pokemon>>
    suspend fun toggleFavorite(id: Int, isFavorite: Boolean)
}
