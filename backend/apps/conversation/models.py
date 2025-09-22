import uuid
from django.db import models
from django.contrib.auth import get_user_model
from apps.accounts.models import Vault

User = get_user_model()


class PhraseCategory(models.TextChoices):
    GREETING = 'greeting', 'Saludo'
    FAMILY = 'family', 'Familia'
    FOOD = 'food', 'Comida'
    LOVE = 'love', 'Amor'
    WISDOM = 'wisdom', 'Sabiduría'
    HUMOR = 'humor', 'Humor'
    STORY = 'story', 'Historia'
    ADVICE = 'advice', 'Consejo'
    OTHER = 'other', 'Otro'


class Phrase(models.Model):
    """
    Modelo para frases típicas familiares
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    text = models.TextField()
    translation = models.TextField(blank=True)  # Traducción si es necesario
    category = models.CharField(max_length=20, choices=PhraseCategory.choices, default=PhraseCategory.OTHER)
    
    # Información del contexto
    context = models.TextField(blank=True)  # Cuándo se usaba esta frase
    person_mentioned = models.CharField(max_length=200, blank=True)  # Quién la decía
    language = models.CharField(max_length=10, default='es')  # Idioma original
    
    # Archivos de audio
    audio_file = models.FileField(upload_to='conversation/audio/', null=True, blank=True)
    audio_duration = models.FloatField(null=True, blank=True)  # Duración en segundos
    
    # Metadatos
    tags = models.JSONField(default=list, blank=True)
    is_favorite = models.BooleanField(default=False)
    usage_count = models.PositiveIntegerField(default=0)  # Cuántas veces se ha reproducido
    
    # Relaciones
    vault = models.ForeignKey(Vault, on_delete=models.CASCADE, related_name='phrases')
    created_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='created_phrases')
    
    # Timestamps
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'phrases'
        verbose_name = 'Frase'
        verbose_name_plural = 'Frases'
        ordering = ['-usage_count', '-created_at']
    
    def __str__(self):
        return f"{self.text[:50]}... - {self.vault.name}"


class PhrasePlayback(models.Model):
    """
    Modelo para registrar reproducciones de frases
    """
    phrase = models.ForeignKey(Phrase, on_delete=models.CASCADE, related_name='playbacks')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='phrase_playbacks')
    played_at = models.DateTimeField(auto_now_add=True)
    duration_played = models.FloatField(null=True, blank=True)  # Cuánto tiempo se reprodujo
    
    class Meta:
        db_table = 'phrase_playbacks'
        verbose_name = 'Reproducción de Frase'
        verbose_name_plural = 'Reproducciones de Frases'
        ordering = ['-played_at']
    
    def __str__(self):
        return f"{self.phrase.text[:30]}... reproducida por {self.user.name}"


class ConversationSession(models.Model):
    """
    Modelo para sesiones de conversación
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    name = models.CharField(max_length=200)
    description = models.TextField(blank=True)
    
    # Configuración de la sesión
    phrases = models.ManyToManyField(Phrase, related_name='conversation_sessions')
    auto_play = models.BooleanField(default=False)
    shuffle_order = models.BooleanField(default=False)
    
    # Estadísticas
    total_playbacks = models.PositiveIntegerField(default=0)
    last_played = models.DateTimeField(null=True, blank=True)
    
    # Relaciones
    vault = models.ForeignKey(Vault, on_delete=models.CASCADE, related_name='conversation_sessions')
    created_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='created_conversation_sessions')
    
    # Timestamps
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'conversation_sessions'
        verbose_name = 'Sesión de Conversación'
        verbose_name_plural = 'Sesiones de Conversación'
        ordering = ['-last_played', '-created_at']
    
    def __str__(self):
        return f"{self.name} - {self.vault.name}"


class ConversationPlayback(models.Model):
    """
    Modelo para registrar reproducciones de sesiones completas
    """
    session = models.ForeignKey(ConversationSession, on_delete=models.CASCADE, related_name='playbacks')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='conversation_playbacks')
    started_at = models.DateTimeField(auto_now_add=True)
    ended_at = models.DateTimeField(null=True, blank=True)
    phrases_played = models.PositiveIntegerField(default=0)
    
    class Meta:
        db_table = 'conversation_playbacks'
        verbose_name = 'Reproducción de Sesión'
        verbose_name_plural = 'Reproducciones de Sesiones'
        ordering = ['-started_at']
    
    def __str__(self):
        return f"Sesión {self.session.name} reproducida por {self.user.name}"
