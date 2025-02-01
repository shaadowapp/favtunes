package com.shaadow.tunes.repository

import com.shaadow.tunes.database.UserDao
import com.shaadow.tunes.database.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun getUser() = userDao.getUser()
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun clearAllUsers() = userDao.clearAllUsers()
}
