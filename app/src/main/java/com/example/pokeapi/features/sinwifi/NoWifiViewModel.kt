package com.example.pokeapi.features.sinwifi

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NoWifiViewModel @Inject constructor() : ViewModel() {
    private val _pokemonName = MutableStateFlow("")
    val pokemonName = _pokemonName.asStateFlow()

    fun setPokemonName(name: String) {
        _pokemonName.value = name
    }
}
