package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.User
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.UserDao
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.PhotoDatabase

class UserRepository(context: Context) {

    private val userDao: UserDao = PhotoDatabase.getDatabase(context).userDao()
    private val database = FirebaseDatabase.getInstance()

    fun getUserByEmail(email: String): LiveData<User> {
        return userDao.getUserByEmail(email)
    }

    suspend fun insert(user: User) {
        userDao.insert(user)
        saveUserToFirebase(user)
    }

    suspend fun delete(user: User) {
        userDao.delete(user)
        deleteUserFromFirebase(user)
    }

    private fun saveUserToFirebase(user: User) {
        val userRef = database.getReference("users").child(user.email.replace(".", "_"))
        userRef.setValue(user).addOnSuccessListener {
            // Usuario subido correctamente a Firebase Database
        }.addOnFailureListener {
            // Error al subir el usuario a Firebase Database
        }
    }

    private fun deleteUserFromFirebase(user: User) {
        val userRef = database.getReference("users").child(user.email.replace(".", "_"))
        userRef.removeValue().addOnSuccessListener {
            // Usuario eliminado correctamente de Firebase Database
        }.addOnFailureListener {
            // Error al eliminar el usuario de Firebase Database
        }
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }
}
