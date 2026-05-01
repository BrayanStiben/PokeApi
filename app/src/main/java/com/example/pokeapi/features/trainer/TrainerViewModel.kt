package com.example.pokeapi.features.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokeapi.data.model.UserEntity
import com.example.pokeapi.domain.repository.AuthRepository
import com.example.pokeapi.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _favorites = MutableStateFlow<List<com.example.pokeapi.domain.model.Pokemon>>(emptyList())
    
    val favoriteCount = _favorites.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val firstFavorite = _favorites.map { it.firstOrNull() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isFlipping = MutableStateFlow(false)
    val isFlipping = _isFlipping.asStateFlow()

    private val _coinResult = MutableStateFlow<Boolean?>(null) // true = Heads (Pokemon), false = Tails (Fire)
    val coinResult = _coinResult.asStateFlow()

    private val _minigameEvent = MutableSharedFlow<String>()
    val minigameEvent = _minigameEvent.asSharedFlow()

    init {
        loadUserData()
        loadFavorites()
    }

    private fun loadUserData() {
        authRepository.getLoggedUser()
            .onEach { _user.value = it }
            .launchIn(viewModelScope)
    }

    private fun loadFavorites() {
        pokemonRepository.getFavoritePokemons()
            .onEach { _favorites.value = it }
            .launchIn(viewModelScope)
    }

    fun playMinigame(userChoiceIsHeads: Boolean) {
        if (_isFlipping.value) return

        viewModelScope.launch {
            _isFlipping.value = true
            _coinResult.value = null // Reset result for animation
            kotlinx.coroutines.delay(2000) 
            
            val resultIsHeads = (0..1).random() == 0
            _coinResult.value = resultIsHeads
            _isFlipping.value = false

            if (userChoiceIsHeads == resultIsHeads) {
                val randomId = (1..1025).random()
                pokemonRepository.getPokemonDetail(randomId).onSuccess {
                    _minigameEvent.emit("¡HAS GANADO! Se ha capturado a ${it.name}")
                    pokemonRepository.toggleFavorite(randomId, true)
                }.onFailure {
                    _minigameEvent.emit("¡HAS GANADO! Pero el Pokémon se escapó antes de registrarlo...")
                }
            } else {
                if (_favorites.value.isNotEmpty()) {
                    val pokeToRemove = _favorites.value.random()
                    _minigameEvent.emit("¡HAS PERDIDO! ${pokeToRemove.name} ha escapado de tu colección")
                    pokemonRepository.toggleFavorite(pokeToRemove.id, false)
                } else {
                    _minigameEvent.emit("¡HAS PERDIDO! Por suerte no tenías Pokémon que pudieran escapar")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
