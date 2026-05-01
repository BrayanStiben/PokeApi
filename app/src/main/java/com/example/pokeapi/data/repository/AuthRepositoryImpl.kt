package com.example.pokeapi.data.repository

import com.example.pokeapi.data.PokeDatabase
import com.example.pokeapi.data.model.UserEntity
import com.example.pokeapi.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val db: PokeDatabase
) : AuthRepository {

    override suspend fun register(user: UserEntity): Result<Unit> {
        return try {
            val existingUser = db.userDao().getUserByTrainerId(user.trainerId)
            if (existingUser != null) {
                Result.failure(Exception("Trainer ID already exists"))
            } else {
                db.userDao().insertUser(user)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(trainerId: String, password: String): Result<UserEntity> {
        return try {
            val user = db.userDao().getUserByTrainerId(trainerId)
            if (user != null && user.password == password) {
                db.userDao().logoutAll()
                db.userDao().updateLoginStatus(trainerId, true)
                Result.success(user.copy(isLogged = true))
            } else {
                Result.failure(Exception("Invalid Trainer ID or Password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        db.userDao().logoutAll()
    }

    override fun getLoggedUser(): Flow<UserEntity?> = flow {
        // Emitir el usuario logueado inicialmente y cada vez que cambie si fuera necesario
        // Para simplicidad en este ejemplo, consultamos directamente
        emit(db.userDao().getLoggedUser())
    }

    override suspend fun resetPassword(trainerId: String, newPassword: String): Result<Unit> {
        return try {
            val user = db.userDao().getUserByTrainerId(trainerId)
            if (user != null) {
                db.userDao().updatePassword(trainerId, newPassword)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Trainer ID not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
