package com.shaadow.tunes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaadow.tunes.database.AppDatabase
import com.shaadow.tunes.database.UserEntity
import com.shaadow.tunes.repository.UserRepository
import com.shaadow.tunes.utils.RandomUtils
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    fun getOrCreateUser(callback: (UserEntity) -> Unit) {
        viewModelScope.launch {
            var user = userRepository.getUser()
            if (user == null) {
                // Generate a new user
                user = UserEntity(
                    publicKey = RandomUtils.generatePublicKey(),
                    privateKey = RandomUtils.generatePrivateKey(),
                    username = RandomUtils.generateRandomUsername(),
                    deviceModel = android.os.Build.MODEL,
                    createdAt = System.currentTimeMillis()
                )
                userRepository.insertUser(user)
            }
            callback(user)
        }
    }
}
