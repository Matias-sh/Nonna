from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from .models import User, Vault, VaultMember


@admin.register(User)
class UserAdmin(BaseUserAdmin):
    list_display = ('email', 'name', 'username', 'is_premium', 'is_active', 'created_at')
    list_filter = ('is_premium', 'is_active', 'is_staff', 'created_at')
    search_fields = ('email', 'name', 'username')
    ordering = ('-created_at',)
    
    fieldsets = (
        (None, {'fields': ('email', 'password')}),
        ('Información Personal', {'fields': ('name', 'username', 'avatar', 'phone', 'birth_date')}),
        ('Permisos', {'fields': ('is_active', 'is_staff', 'is_superuser', 'is_premium', 'groups', 'user_permissions')}),
        ('Fechas Importantes', {'fields': ('last_login', 'date_joined')}),
    )
    
    add_fieldsets = (
        (None, {
            'classes': ('wide',),
            'fields': ('email', 'name', 'username', 'password1', 'password2'),
        }),
    )


@admin.register(Vault)
class VaultAdmin(admin.ModelAdmin):
    list_display = ('name', 'owner', 'is_public', 'created_at')
    list_filter = ('is_public', 'created_at')
    search_fields = ('name', 'owner__name', 'owner__email')
    ordering = ('-created_at',)


@admin.register(VaultMember)
class VaultMemberAdmin(admin.ModelAdmin):
    list_display = ('user', 'vault', 'role', 'joined_at')
    list_filter = ('role', 'joined_at')
    search_fields = ('user__name', 'user__email', 'vault__name')
    ordering = ('-joined_at',)
