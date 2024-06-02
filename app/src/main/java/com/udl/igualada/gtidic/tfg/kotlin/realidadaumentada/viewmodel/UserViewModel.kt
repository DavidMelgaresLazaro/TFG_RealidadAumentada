package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.User
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository = UserRepository(application)

    fun getUserByEmail(email: String): LiveData<User> {
        return repository.getUserByEmail(email)
    }

    fun insert(user: User) {
        viewModelScope.launch {
            repository.insert(user)
        }
    }

    fun delete(user: User) {
        viewModelScope.launch {
            repository.delete(user)
        }
    }

    fun getCurrentFirebaseUser() = repository.getCurrentFirebaseUser()
}
