import uuid
from django.db import models
from django.contrib.auth import get_user_model
from apps.accounts.models import Vault

User = get_user_model()


class MemoryType(models.TextChoices):
    PHOTO = 'photo', 'Foto'
    AUDIO = 'audio', 'Audio'
    VIDEO = 'video', 'Video'
    RECIPE = 'recipe', 'Receta'
    NOTE = 'note', 'Nota'
    STORY = 'story', 'Historia'


class Memory(models.Model):
    """
    Modelo para recuerdos familiares
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    title = models.CharField(max_length=200)
    description = models.TextField(blank=True)
    type = models.CharField(max_length=20, choices=MemoryType.choices, default=MemoryType.PHOTO)
    
    # Archivos multimedia
    photo = models.ImageField(upload_to='memories/photos/', null=True, blank=True)
    audio = models.FileField(upload_to='memories/audio/', null=True, blank=True)
    video = models.FileField(upload_to='memories/videos/', null=True, blank=True)
    
    # Metadatos
    date_taken = models.DateTimeField(null=True, blank=True)
    location = models.CharField(max_length=200, blank=True)
    tags = models.JSONField(default=list, blank=True)
    
    # Relaciones
    vault = models.ForeignKey(Vault, on_delete=models.CASCADE, related_name='memories')
    created_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='created_memories')
    
    # Timestamps
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'memories'
        verbose_name = 'Recuerdo'
        verbose_name_plural = 'Recuerdos'
        ordering = ['-date_taken', '-created_at']
    
    def __str__(self):
        return f"{self.title} - {self.vault.name}"


class MemoryComment(models.Model):
    """
    Modelo para comentarios en recuerdos
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    memory = models.ForeignKey(Memory, on_delete=models.CASCADE, related_name='comments')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='memory_comments')
    text = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'memory_comments'
        verbose_name = 'Comentario'
        verbose_name_plural = 'Comentarios'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"Comentario de {self.user.name} en {self.memory.title}"


class MemoryLike(models.Model):
    """
    Modelo para likes en recuerdos
    """
    memory = models.ForeignKey(Memory, on_delete=models.CASCADE, related_name='likes')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='memory_likes')
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'memory_likes'
        unique_together = ['memory', 'user']
        verbose_name = 'Like'
        verbose_name_plural = 'Likes'
    
    def __str__(self):
        return f"Like de {self.user.name} en {self.memory.title}"


class MemoryShare(models.Model):
    """
    Modelo para compartir recuerdos
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    memory = models.ForeignKey(Memory, on_delete=models.CASCADE, related_name='shares')
    shared_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='shared_memories')
    shared_with = models.ForeignKey(User, on_delete=models.CASCADE, related_name='received_memories')
    message = models.TextField(blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'memory_shares'
        unique_together = ['memory', 'shared_with']
        verbose_name = 'Compartir'
        verbose_name_plural = 'Compartidos'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.memory.title} compartido por {self.shared_by.name} con {self.shared_with.name}"
