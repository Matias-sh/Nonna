package com.cocido.nonna.ui.conversation

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.domain.model.Phrase
import com.cocido.nonna.domain.usecase.GetPhrasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel para el fragment de conversación
 */
@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val getPhrasesUseCase: GetPhrasesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ConversationUiState>(ConversationUiState.Idle)
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()
    
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    
    fun initializeTts(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            isTtsInitialized = status == TextToSpeech.SUCCESS
            if (isTtsInitialized) {
                textToSpeech?.language = Locale("es", "ES")
                textToSpeech?.setSpeechRate(0.8f) // Velocidad ligeramente más lenta
            }
        }
    }
    
    fun loadPhrases() {
        viewModelScope.launch {
            _uiState.value = ConversationUiState.Loading
            
            getPhrasesUseCase()
                .onSuccess { phrases ->
                    _uiState.value = ConversationUiState.Success(phrases)
                }
                .onFailure { exception ->
                    _uiState.value = ConversationUiState.Error(
                        message = exception.message ?: "Error al cargar las frases"
                    )
                }
        }
    }
    
    fun playPhrase(phrase: Phrase) {
        if (!isTtsInitialized) {
            _uiState.value = ConversationUiState.Error("TTS no está inicializado")
            return
        }
        
        _uiState.value = ConversationUiState.Playing(phrase)
        
        textToSpeech?.speak(
            phrase.text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "phrase_${phrase.id}"
        )
        
        // Simular finalización de reproducción después de un delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(phrase.text.length * 100L) // Estimación basada en longitud del texto
            _uiState.value = ConversationUiState.Success(
                (_uiState.value as? ConversationUiState.Success)?.phrases ?: emptyList()
            )
        }
    }
    
    fun stopPlayback() {
        textToSpeech?.stop()
        _uiState.value = ConversationUiState.Success(
            (_uiState.value as? ConversationUiState.Success)?.phrases ?: emptyList()
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
    }
}

/**
 * Estados de la UI para la conversación
 */
sealed class ConversationUiState {
    object Idle : ConversationUiState()
    object Loading : ConversationUiState()
    data class Success(val phrases: List<Phrase>) : ConversationUiState()
    data class Error(val message: String) : ConversationUiState()
    data class Playing(val phrase: Phrase) : ConversationUiState()
}