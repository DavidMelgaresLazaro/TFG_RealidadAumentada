package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Photo::class, User::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: PhotoDatabase? = null

        fun getDatabase(context: Context): PhotoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoDatabase::class.java,
                    "photo_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
