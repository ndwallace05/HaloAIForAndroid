package xyz.haloai.haloai_android_productivity.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.data.local.dao.NoteDao
import xyz.haloai.haloai_android_productivity.data.local.entities.Note
import xyz.haloai.haloai_android_productivity.ui.viewmodel.LlmViewModel

class NotesDbRepository(private val noteDao: NoteDao): KoinComponent {

    val llmViewModel: LlmViewModel by inject() // To make AI calls

    suspend fun insert(title: String, content: String): Long = withContext(Dispatchers.IO) {
        val summary = getSummaryForNote(title, content)
        val noteToInsert = Note(
            id = 0,
            title = title,
            summary = summary,
            content = content
        )
        noteDao.insert(noteToInsert)
    }

    suspend fun getAllNotes(): List<Note> = withContext(Dispatchers.IO) {
        return@withContext noteDao.getAll()
    }

    suspend fun getNoteById(id: Long): Note = withContext(Dispatchers.IO) {
        return@withContext noteDao.getById(id)
    }

    suspend fun deleteAllNotes() = withContext(Dispatchers.IO) {
        noteDao.deleteAll()
    }

    suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteById(id)
    }

    suspend fun updateNoteContentById(id: Long, title: String? = null, content: String? = null) = withContext(Dispatchers.IO) {
        if (title == null && content == null) return@withContext
        val note = getNoteById(id)
        val titleToUse = title ?: note.title
        val contentToUse = content ?: note.content
        val updatedSummary = getSummaryForNote(titleToUse, contentToUse)
        noteDao.updateById(id, titleToUse, contentToUse, updatedSummary)
    }

    suspend fun appendContentToNoteById(id: Long, content: String) = withContext(Dispatchers.IO) {
        val note = getNoteById(id)
        val updatedContent = note.content + "\n" + content
        val updatedSummary = getSummaryForNote(note.title, updatedContent)
        noteDao.updateById(id, note.title, updatedContent, updatedSummary)
    }

    suspend fun getSummaryForNote(title: String, content: String): String = withContext(Dispatchers.IO) {
        val promptText = "Generate a summary of the given note. Keep the summary short (1-2 lines" +
                " max), and make sure it captures the essence of the note. Generate only the " +
                "summary, and no additional text, formatting, or other content."
        val contextText = "Title: $title\nContent: $content\n\n"
        return@withContext llmViewModel.getResponse(promptText, contextText)
    }

    suspend fun addToSomeNote(content: String, extraInfo: String?) = withContext(Dispatchers.IO) {
        val allNotes = getAllNotes()
        val promptText = "You are a helpful assistant. Given a list of notes the user has (alongwith their summaries), and a some content they want to add, reply with the index of the note this best fits in. Reply only with the index, and no other text. Reply with -1 if the content should be added as a new note."
        var contextText = "Notes:\n"
        // Index. Note Title: Note Summary and so on...
        contextText += allNotes.mapIndexed { index, note -> "${index + 1}. ${note.title}: ${note.summary}" }.joinToString("\n")
        contextText += "\n\nContent to add: $content\n"
        if (extraInfo != null) {
            contextText += "Extra Info: $extraInfo\n"
        }
        val response = llmViewModel.getResponse(promptText, contextText)
        val noteIndex = response.trim().toInt()
        if (noteIndex == -1) {
            insert("New Note", content)
        } else {
            val noteId = allNotes[noteIndex - 1].id
            appendContentToNoteById(noteId, "\n" + content + "\n")
        }
    }
}