package com.example.pokeapi.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.pokeapi.data.model.*
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        PokemonEntity::class, 
        PokemonStatEntity::class, 
        PokemonMoveEntity::class, 
        MoveDetailEntity::class,
        RemoteKeysEntity::class,
        UserEntity::class
    ],
    version = 6, // Incrementado a 6 para incluir UserEntity
    exportSchema = false
)
abstract class PokeDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun userDao(): UserDao
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user_table WHERE trainerId = :trainerId LIMIT 1")
    suspend fun getUserByTrainerId(trainerId: String): UserEntity?

    @Query("SELECT * FROM user_table WHERE isLogged = 1 LIMIT 1")
    suspend fun getLoggedUser(): UserEntity?

    @Query("UPDATE user_table SET isLogged = :isLogged WHERE trainerId = :trainerId")
    suspend fun updateLoginStatus(trainerId: String, isLogged: Boolean)

    @Query("UPDATE user_table SET isLogged = 0")
    suspend fun logoutAll()
    
    @Query("UPDATE user_table SET password = :newPassword WHERE trainerId = :trainerId")
    suspend fun updatePassword(trainerId: String, newPassword: String)
}

@Dao
interface PokemonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemons: List<PokemonEntity>)

    @Query("SELECT * FROM pokemon_table ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, PokemonEntity>

    @Query("""
        SELECT * FROM pokemon_table 
        WHERE (LOWER(name) LIKE '%' || LOWER(:query) || '%' OR CAST(id AS TEXT) LIKE '%' || :query || '%')
        AND (:type IS NULL OR LOWER(types) LIKE '%' || LOWER(:type) || '%')
        AND (:category IS NULL OR LOWER(name) LIKE '%-' || LOWER(:category) || '%')
        ORDER BY id ASC
    """)
    fun searchPokemons(query: String, type: String?, category: String?): PagingSource<Int, PokemonEntity>

    @Query("DELETE FROM pokemon_table WHERE isFavorite = 0")
    suspend fun clearNonFavorites()

    @Query("SELECT id FROM pokemon_table WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Int>

    @Query("SELECT * FROM pokemon_table WHERE id = :id")
    suspend fun getPokemonById(id: Int): PokemonEntity?
    
    @Query("SELECT * FROM pokemon_table WHERE id = :id")
    fun getPokemonByIdFlow(id: Int): Flow<PokemonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: List<PokemonStatEntity>)

    @Query("SELECT * FROM pokemon_stat_table WHERE pokemonId = :pokemonId")
    suspend fun getStatsForPokemon(pokemonId: Int): List<PokemonStatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoves(moves: List<PokemonMoveEntity>)

    @Query("SELECT * FROM pokemon_move_table WHERE pokemonId = :pokemonId")
    suspend fun getMovesForPokemon(pokemonId: Int): List<PokemonMoveEntity>

    @Query("DELETE FROM pokemon_move_table WHERE pokemonId = :pokemonId")
    suspend fun deleteMovesForPokemon(pokemonId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoveDetail(detail: MoveDetailEntity)

    @Query("SELECT * FROM move_detail_cache WHERE name = :name")
    suspend fun getMoveDetail(name: String): MoveDetailEntity?

    @Query("UPDATE pokemon_table SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("SELECT * FROM pokemon_table WHERE isFavorite = 1 ORDER BY id ASC")
    fun getFavoritePokemons(): Flow<List<PokemonEntity>>
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
