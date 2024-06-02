package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos")
    fun getAllPhotos(): LiveData<List<Photo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)
}
