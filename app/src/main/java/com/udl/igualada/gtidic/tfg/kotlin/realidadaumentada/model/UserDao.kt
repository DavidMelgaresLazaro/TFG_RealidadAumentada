package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)
}
