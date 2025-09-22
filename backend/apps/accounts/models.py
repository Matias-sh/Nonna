import uuid
from django.contrib.auth.models import AbstractUser
from django.db import models
from django.core.validators import RegexValidator


class User(AbstractUser):
    """
    Modelo de usuario personalizado para Nonna
    """
    email = models.EmailField(unique=True)
    name = models.CharField(max_length=100)
    avatar = models.ImageField(upload_to='avatars/', null=True, blank=True)
    phone = models.CharField(
        max_length=15,
        validators=[RegexValidator(regex=r'^\+?1?\d{9,15}$')],
        blank=True
    )
    birth_date = models.DateField(null=True, blank=True)
    is_premium = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['username', 'name']
    
    class Meta:
        db_table = 'users'
        verbose_name = 'Usuario'
        verbose_name_plural = 'Usuarios'
    
    def __str__(self):
        return f"{self.name} ({self.email})"


class Vault(models.Model):
    """
    Modelo para cofres de recuerdos familiares
    """
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    name = models.CharField(max_length=100)
    description = models.TextField(blank=True)
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='vaults')
    is_public = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'vaults'
        verbose_name = 'Cofre'
        verbose_name_plural = 'Cofres'
        ordering = ['-created_at']
    
    def __str__(self):
        return f"{self.name} - {self.owner.name}"


class VaultMember(models.Model):
    """
    Modelo para miembros de un cofre (compartir cofres)
    """
    vault = models.ForeignKey(Vault, on_delete=models.CASCADE, related_name='members')
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='vault_memberships')
    role = models.CharField(
        max_length=20,
        choices=[
            ('owner', 'Propietario'),
            ('admin', 'Administrador'),
            ('member', 'Miembro'),
            ('viewer', 'Solo lectura'),
        ],
        default='member'
    )
    joined_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'vault_members'
        unique_together = ['vault', 'user']
        verbose_name = 'Miembro del Cofre'
        verbose_name_plural = 'Miembros del Cofre'
    
    def __str__(self):
        return f"{self.user.name} en {self.vault.name} ({self.role})"
