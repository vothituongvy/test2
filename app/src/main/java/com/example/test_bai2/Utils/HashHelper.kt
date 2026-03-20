package com.example.test_bai2.Utils

import org.mindrot.jbcrypt.BCrypt

object HashHelper {
    fun hashPassword(password: String):
            String = BCrypt.hashpw(password, BCrypt.gensalt())

    fun checkPassword(password: String, hashed: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashed)
        } catch (e: Exception) {
            false
        }
    }
}