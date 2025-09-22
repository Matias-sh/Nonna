from django.contrib import admin
from .models import Memory, MemoryComment, MemoryLike, MemoryShare


@admin.register(Memory)
class MemoryAdmin(admin.ModelAdmin):
    list_display = ('title', 'type', 'vault', 'created_by', 'date_taken', 'created_at')
    list_filter = ('type', 'vault', 'created_at', 'date_taken')
    search_fields = ('title', 'description', 'created_by__name', 'vault__name')
    ordering = ('-created_at',)
    readonly_fields = ('id', 'created_at', 'updated_at')


@admin.register(MemoryComment)
class MemoryCommentAdmin(admin.ModelAdmin):
    list_display = ('memory', 'user', 'created_at')
    list_filter = ('created_at',)
    search_fields = ('memory__title', 'user__name', 'text')
    ordering = ('-created_at',)
    readonly_fields = ('id', 'created_at', 'updated_at')


@admin.register(MemoryLike)
class MemoryLikeAdmin(admin.ModelAdmin):
    list_display = ('memory', 'user', 'created_at')
    list_filter = ('created_at',)
    search_fields = ('memory__title', 'user__name')
    ordering = ('-created_at',)
    readonly_fields = ('created_at',)


@admin.register(MemoryShare)
class MemoryShareAdmin(admin.ModelAdmin):
    list_display = ('memory', 'shared_by', 'shared_with', 'created_at')
    list_filter = ('created_at',)
    search_fields = ('memory__title', 'shared_by__name', 'shared_with__name')
    ordering = ('-created_at',)
    readonly_fields = ('id', 'created_at',)
