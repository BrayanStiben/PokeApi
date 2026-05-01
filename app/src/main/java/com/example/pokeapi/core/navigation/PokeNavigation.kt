package com.example.pokeapi.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pokeapi.features.auth.ForgotPasswordScreen
import com.example.pokeapi.features.auth.LoginScreen
import com.example.pokeapi.features.auth.RegisterScreen
import com.example.pokeapi.features.pokemon_detail.PokemonDetailScreen
import com.example.pokeapi.features.pokemon_list.PokemonListScreen
import com.example.pokeapi.features.favorites.FavoritesScreen
import com.example.pokeapi.features.trainer.TrainerScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object PokemonList : Screen("pokemon_list")
    object PokemonDetail : Screen("pokemon_detail/{pokemonId}/{pokemonName}") {
        fun createRoute(pokemonId: Int, pokemonName: String) = "pokemon_detail/$pokemonId/$pokemonName"
    }
    object Favorites : Screen("favorites")
    object Trainer : Screen("trainer")
}

@Composable
fun PokeNavHost(
    navController: NavHostController,
    viewModel: NavViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsState()

    if (startDestination == null) return // O un splash screen

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.PokemonList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onResetSuccess = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.PokemonList.route) {
            PokemonListScreen(
                onPokemonClick = { pokemonId, pokemonName ->
                    navController.navigate(Screen.PokemonDetail.createRoute(pokemonId, pokemonName))
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
                navArgument("pokemonId") { type = NavType.IntType },
                navArgument("pokemonName") { type = NavType.StringType }
            )
        ) {
            PokemonDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onPokemonClick = { pokemonId, pokemonName ->
                    navController.navigate(Screen.PokemonDetail.createRoute(pokemonId, pokemonName))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Trainer.route) {
            TrainerScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}