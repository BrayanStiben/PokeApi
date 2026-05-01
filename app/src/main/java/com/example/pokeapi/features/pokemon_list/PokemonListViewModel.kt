package com.example.pokeapi.features.pokemon_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.pokeapi.domain.model.Pokemon
import com.example.pokeapi.domain.repository.PokemonRepository
import com.example.pokeapi.core.utils.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonListState())
    val state = _state.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemons: Flow<PagingData<Pokemon>> = combine(
        _state.map { it.searchQuery }.distinctUntilChanged().debounce(500),
        _state.map { it.selectedType }.distinctUntilChanged(),
        _state.map { it.selectedCategory }.distinctUntilChanged()
    ) { query, type, category ->
        Triple(query, type, category)
    }.flatMapLatest { (query, type, category) ->
        repository.getPokemons(query, type, category)
    }.cachedIn(viewModelScope)

    init {
        observeNetworkStatus()
        loadTypes()
    }

    private fun observeNetworkStatus() {
        connectivityObserver.observe()
            .onEach { status ->
                _state.update { it.copy(networkStatus = status) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadTypes() {
        viewModelScope.launch {
            repository.getTypes().onSuccess { types ->
                _state.update { it.copy(types = types) }
            }
        }
    }

    fun onEvent(event: PokemonListEvent) {
        when (event) {
            is PokemonListEvent.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is PokemonListEvent.OnTypeSelected -> {
                _state.update { it.copy(selectedType = event.type, isTypeDropdownExpanded = false) }
            }
            is PokemonListEvent.OnCategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
            PokemonListEvent.OnToggleTypeDropdown -> {
                _state.update { it.copy(isTypeDropdownExpanded = !it.isTypeDropdownExpanded) }
            }
            is PokemonListEvent.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(event.pokemonId, event.isFavorite)
                }
            }
        }
    }
}
