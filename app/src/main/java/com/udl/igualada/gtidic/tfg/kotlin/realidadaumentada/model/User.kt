package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val uid: String,
    val displayName: String?,
    val photoUrl: String?
)
