package com.example.pokeapi.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pokeapi.features.pokemon_detail.PokemonDetailScreen
import com.example.pokeapi.features.pokemon_list.PokemonListScreen
import com.example.pokeapi.features.favorites.FavoritesScreen
import com.example.pokeapi.features.trainer.TrainerScreen

sealed class Screen(val route: String) {
    object PokemonList : Screen("pokemon_list")
    object PokemonDetail : Screen("pokemon_detail/{pokemonId}") {
        fun createRoute(pokemonId: Int) = "pokemon_detail/$pokemonId"
    }
    object Favorites : Screen("favorites")
    object Trainer : Screen("trainer")
}

@Composable
fun PokeNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.PokemonList.route
    ) {
        composable(Screen.PokemonList.route) {
            PokemonListScreen(
                onPokemonClick = { pokemonId ->
                    navController.navigate(Screen.PokemonDetail.createRoute(pokemonId))
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onTrainerClick = {
                    navController.navigate(Screen.Trainer.route)
                }
            )
        }
        composable(
            route = Screen.PokemonDetail.route,
            arguments = listOf(
                navArgument("pokemonId") { type = NavType.IntType }
            )
        ) {
            PokemonDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Favorites.route) {
          //  FavoritesScreen()
        }
        composable(Screen.Trainer.route) {
            TrainerScreen()
        }
    }
}