package com.example.pokeapi.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    val favoritePokemons: StateFlow<List<Pokemon>> = repository.getFavoritePokemons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onToggleFavorite(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, isFavorite)
        }
    }
}
