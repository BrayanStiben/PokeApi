package com.example.pokeapi.domain.repository

import com.example.pokeapi.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(user: UserEntity): Result<Unit>
    suspend fun login(trainerId: String, password: String): Result<UserEntity>
    suspend fun logout()
    fun getLoggedUser(): Flow<UserEntity?>
    suspend fun resetPassword(trainerId: String, newPassword: String): Result<Unit>
}
