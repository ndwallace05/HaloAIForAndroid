package xyz.haloai.haloai_android_productivity.ui.viewmodel

import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.data.local.entities.Note
import xyz.haloai.haloai_android_productivity.data.local.repository.NotesDbRepository

class NotesDbViewModel(private val repository: NotesDbRepository) : ViewModel() {

    suspend fun insert(title: String, content: String): Long {
        return repository.insert(title, content)
    }

    suspend fun getAllNotes(): List<Note> {
        return repository.getAllNotes()
    }

    suspend fun getNoteById(id: Long): Note {
        return repository.getNoteById(id)
    }

    suspend fun deleteAllNotes() {
        repository.deleteAllNotes()
    }

    suspend fun deleteNoteById(id: Long) {
        repository.deleteNoteById(id)
    }

    suspend fun updateNoteContentById(id: Long, title: String? = null, content: String? = null) {
        repository.updateNoteContentById(id, title, content)
    }

    suspend fun appendContentToNoteById(id: Long, content: String) {
        repository.appendContentToNoteById(id, content)
    }

    suspend fun addToSomeNote(content: String, extraInfo: String? = null) {
        repository.addToSomeNote(content, extraInfo)
    }

}