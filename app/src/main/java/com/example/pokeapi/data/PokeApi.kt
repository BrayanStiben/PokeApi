package com.example.pokeapi.data

import com.example.pokeapi.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApi {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonListResponse

    @GET("pokemon/{idOrName}")
    suspend fun getPokemonDetail(
        @Path("idOrName") idOrName: String
    ): PokemonDetailDto

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(
        @Path("id") id: Int
    ): PokemonSpeciesDto

    @GET("type")
    suspend fun getTypes(): TypeListResponse

    @GET("type/{name}")
    suspend fun getPokemonByType(
        @Path("name") name: String
    ): TypeDetailResponse

    companion object {
        const val BASE_URL = "https://pokeapi.co/api/v2/"
    }
}
