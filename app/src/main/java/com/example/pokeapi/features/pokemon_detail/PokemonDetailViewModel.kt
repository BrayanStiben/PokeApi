package com.example.pokeapi.features.pokemon_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokeapi.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonDetailState())
    val state = _state.asStateFlow()

    private val pokemonId: Int? = savedStateHandle["pokemonId"]

    init {
        loadPokemonDetail()
    }

    fun onEvent(event: PokemonDetailEvent) {
        when (event) {
            PokemonDetailEvent.OnRetry -> loadPokemonDetail()
            PokemonDetailEvent.OnBackClick -> { /* Handled by navigation in Screen */ }
            is PokemonDetailEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(event.pokemonId, event.isFavorite)
                    // Actualizar el estado local para reflejar el cambio inmediatamente
                    _state.update { currentState ->
                        currentState.copy(
                            pokemon = currentState.pokemon?.copy(isFavorite = event.isFavorite)
                        )
                    }
                }
            }
        }
    }

    private fun loadPokemonDetail() {
        val id = pokemonId ?: return
        val name: String = savedStateHandle["pokemonName"] ?: ""
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, isConnectionError = false, pokemonName = name) }
            repository.getPokemonDetail(id)
                .onSuccess { pokemon ->
                    _state.update { it.copy(pokemon = pokemon, isLoading = false) }
                }
                .onFailure { error ->
                    val isConnError = error is IOException || 
                                     error.message?.contains("Unable to resolve host") == true
                    
                    _state.update { it.copy(
                        error = error.message ?: "Unknown error", 
                        isLoading = false,
                        isConnectionError = isConnError
                    ) }
                }
        }
    }
}
