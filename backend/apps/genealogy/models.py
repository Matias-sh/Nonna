import uuid
from django.db import models
from django.contrib.auth import get_user_model
from apps.accounts.models import Vault

User = get_user_model()


class RelationType(models.TextChoices):
    PARENT = 'parent', 'Padre/Madre'
    CHILD = 'child', 'Hijo/Hija'
    SIBLING = 'sibling', 'Hermano/Hermana'
    SPOUSE = 'spouse', 'Esposo/Esposa'
    GRANDPARENT = 'grandparent', 'Abuelo/Abuela'
    GRANDCHILD = 'grandchild', 'Nieto/Nieta'
    UNCLE_AUNT = 'uncle_aunt', 'Tío/Tía'
    NEPHEW_NIECE = 'nephew_niece', 'Sobrino/Sobrina'
    COUSIN = 'cousin', 'Primo/Prima'
    OTHER = 'other', 'Otro'


class Person(models.Model):
    """
    Modelo para personas en el árbol genealógico
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    first_name = models.CharField(max_length=100)
    last_name = models.CharField(max_length=100)
    middle_name = models.CharField(max_length=100, blank=True)
    
    # Información personal
    birth_date = models.DateField(null=True, blank=True)
    death_date = models.DateField(null=True, blank=True)
    birth_place = models.CharField(max_length=200, blank=True)
    death_place = models.CharField(max_length=200, blank=True)
    
    # Información de contacto
    email = models.EmailField(blank=True)
    phone = models.CharField(max_length=20, blank=True)
    address = models.TextField(blank=True)
    
    # Archivos
    photo = models.ImageField(upload_to='genealogy/photos/', null=True, blank=True)
    documents = models.JSONField(default=list, blank=True)  # Lista de documentos
    
    # Información adicional
    occupation = models.CharField(max_length=200, blank=True)
    notes = models.TextField(blank=True)
    is_living = models.BooleanField(default=True)
    
    # Relaciones
    vault = models.ForeignKey(Vault, on_delete=models.CASCADE, related_name='persons')
    created_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='created_persons')
    
    # Timestamps
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'persons'
        verbose_name = 'Persona'
        verbose_name_plural = 'Personas'
        ordering = ['last_name', 'first_name']
    
    def __str__(self):
        return f"{self.full_name} - {self.vault.name}"
    
    @property
    def full_name(self):
        if self.middle_name:
            return f"{self.first_name} {self.middle_name} {self.last_name}"
        return f"{self.first_name} {self.last_name}"
    
    @property
    def age(self):
        if self.birth_date:
            from datetime import date
            today = date.today()
            if self.death_date:
                return (self.death_date - self.birth_date).days // 365
            return (today - self.birth_date).days // 365
        return None


class Relation(models.Model):
    """
    Modelo para relaciones entre personas
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    person1 = models.ForeignKey(Person, on_delete=models.CASCADE, related_name='relations_from')
    person2 = models.ForeignKey(Person, on_delete=models.CASCADE, related_name='relations_to')
    relation_type = models.CharField(max_length=20, choices=RelationType.choices)
    
    # Información adicional de la relación
    start_date = models.DateField(null=True, blank=True)  # Fecha de matrimonio, etc.
    end_date = models.DateField(null=True, blank=True)    # Fecha de divorcio, etc.
    notes = models.TextField(blank=True)
    
    # Timestamps
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'relations'
        verbose_name = 'Relación'
        verbose_name_plural = 'Relaciones'
        unique_together = ['person1', 'person2', 'relation_type']
    
    def __str__(self):
        return f"{self.person1.full_name} - {self.get_relation_type_display()} - {self.person2.full_name}"


class PersonMemory(models.Model):
    """
    Modelo para asociar recuerdos con personas
    """
    person = models.ForeignKey(Person, on_delete=models.CASCADE, related_name='memories')
    memory = models.ForeignKey('memories.Memory', on_delete=models.CASCADE, related_name='persons')
    role = models.CharField(
        max_length=50,
        choices=[
            ('subject', 'Sujeto principal'),
            ('mentioned', 'Mencionado'),
            ('photographer', 'Fotógrafo'),
            ('narrator', 'Narrador'),
        ],
        default='subject'
    )
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'person_memories'
        unique_together = ['person', 'memory']
        verbose_name = 'Recuerdo de Persona'
        verbose_name_plural = 'Recuerdos de Personas'
    
    def __str__(self):
        return f"{self.person.full_name} en {self.memory.title} ({self.role})"
