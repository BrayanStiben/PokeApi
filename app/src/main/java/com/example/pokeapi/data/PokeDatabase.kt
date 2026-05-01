package com.example.pokeapi.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.pokeapi.data.model.PokemonEntity
import com.example.pokeapi.data.model.PokemonStatEntity
import com.example.pokeapi.data.model.RemoteKeysEntity

@Database(
    entities = [PokemonEntity::class, PokemonStatEntity::class, RemoteKeysEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PokeDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun remoteKeysDao(): RemoteKeysDao
}

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemons: List<PokemonEntity>)

    @Query("SELECT * FROM pokemon_table ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, PokemonEntity>

    @Query("""
        SELECT * FROM pokemon_table 
        WHERE (name LIKE '%' || :query || '%' OR CAST(id AS TEXT) LIKE '%' || :query || '%')
        AND (:type IS NULL OR types LIKE '%' || :type || '%')
        ORDER BY id ASC
    """)
    fun searchPokemons(query: String, type: String?): PagingSource<Int, PokemonEntity>

    @Query("DELETE FROM pokemon_table")
    suspend fun clearAll()

    @Query("SELECT * FROM pokemon_table WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: List<PokemonStatEntity>)

    @Query("SELECT * FROM pokemon_stat_table WHERE pokemonId = :pokemonId")
    suspend fun getStatsForPokemon(pokemonId: Int): List<PokemonStatEntity>
}

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeysEntity>)

    @Query("SELECT * FROM remote_keys WHERE pokemonId = :pokemonId")
    suspend fun remoteKeysPokemonId(pokemonId: Int): RemoteKeysEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}
