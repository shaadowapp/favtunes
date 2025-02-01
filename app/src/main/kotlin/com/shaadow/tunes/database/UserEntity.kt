package com.shaadow.tunes.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val publicKey: String, // Public key as primary identifier
    val privateKey: String,
    val username: String,
    val deviceModel: String,
    val createdAt: Long
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(createdAt))
    }
}