package xyz.haloai.haloai_android_productivity.ui.viewmodel

import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.data.local.entities.EnumValType
import xyz.haloai.haloai_android_productivity.data.local.repository.MiscInfoDbRepository

class MiscInfoDbViewModel(private val repository: MiscInfoDbRepository) : ViewModel() {

    suspend fun updateOrCreate(key: String, value: Any, type: EnumValType = EnumValType.STRING) {
        repository.updateOrCreate(key, value, type)
    }

    suspend fun get(key: String): Any? {
        return repository.get(key)
    }

    suspend fun delete(key: String) {
        repository.delete(key)
    }

}