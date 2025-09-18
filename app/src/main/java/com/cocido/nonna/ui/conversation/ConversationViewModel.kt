package com.cocido.nonna.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Phrase
import com.cocido.nonna.domain.model.VaultId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el modo conversación
 * Maneja la carga de frases y la reproducción de audio/TTS
 */
@HiltViewModel
class ConversationViewModel @Inject constructor(
    // TODO: Inyectar repositorios cuando estén implementados
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ConversationUiState>(ConversationUiState.Loading)
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()
    
    private var currentPlayingPhrase: Phrase? = null
    
    fun loadPhrases() {
        viewModelScope.launch {
            _uiState.value = ConversationUiState.Loading
            
            try {
                // TODO: Implementar llamada al repositorio
                // val phrases = phraseRepository.getPhrasesByVault(currentVaultId)
                
                // Simulación temporal con datos dummy
                val phrases = createDummyPhrases()
                
                _uiState.value = ConversationUiState.Success(phrases = phrases)
                
            } catch (e: Exception) {
                _uiState.value = ConversationUiState.Error(
                    message = e.message ?: "Error al cargar las frases"
                )
            }
        }
    }
    
    fun playPhrase(phrase: Phrase) {
        viewModelScope.launch {
            currentPlayingPhrase = phrase
            
            try {
                // TODO: Implementar reproducción de audio o TTS
                if (phrase.audioLocalPath != null || phrase.audioRemoteUrl != null) {
                    // Reproducir audio real
                    // audioPlayer.play(phrase.audioLocalPath ?: phrase.audioRemoteUrl)
                } else {
                    // Usar TextToSpeech
                    // textToSpeech.speak(phrase.text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
                
                _uiState.value = ConversationUiState.Playing(phrase = phrase)
                
            } catch (e: Exception) {
                _uiState.value = ConversationUiState.Error(
                    message = "Error al reproducir la frase: ${e.message}"
                )
            }
        }
    }
    
    private fun createDummyPhrases(): List<Phrase> {
        return listOf(
            Phrase(
                id = "phrase1",
                vaultId = VaultId("vault1"),
                text = "¡Ay, mi vida!",
                audioLocalPath = null,
                audioRemoteUrl = null,
                personId = com.cocido.nonna.domain.model.PersonId("person1"),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Phrase(
                id = "phrase2",
                vaultId = VaultId("vault1"),
                text = "En mi época las cosas eran diferentes",
                audioLocalPath = "/path/to/audio2.m4a",
                audioRemoteUrl = null,
                personId = com.cocido.nonna.domain.model.PersonId("person1"),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Phrase(
                id = "phrase3",
                vaultId = VaultId("vault1"),
                text = "¡Qué rico está esto!",
                audioLocalPath = null,
                audioRemoteUrl = null,
                personId = com.cocido.nonna.domain.model.PersonId("person2"),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Phrase(
                id = "phrase4",
                vaultId = VaultId("vault1"),
                text = "Vení que te cuento una historia",
                audioLocalPath = "/path/to/audio4.m4a",
                audioRemoteUrl = null,
                personId = com.cocido.nonna.domain.model.PersonId("person1"),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            Phrase(
                id = "phrase5",
                vaultId = VaultId("vault1"),
                text = "¡Dios mío, qué lindo día!",
                audioLocalPath = null,
                audioRemoteUrl = null,
                personId = com.cocido.nonna.domain.model.PersonId("person3"),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}

/**
 * Estados de la UI para el modo conversación
 */
sealed class ConversationUiState {
    object Loading : ConversationUiState()
    data class Success(val phrases: List<Phrase>) : ConversationUiState()
    data class Error(val message: String) : ConversationUiState()
    data class Playing(val phrase: Phrase) : ConversationUiState()
}


