from django.contrib import admin
from .models import Phrase, PhrasePlayback, ConversationSession, ConversationPlayback


@admin.register(Phrase)
class PhraseAdmin(admin.ModelAdmin):
    list_display = ('text', 'category', 'person_mentioned', 'usage_count', 'is_favorite', 'vault', 'created_at')
    list_filter = ('category', 'is_favorite', 'vault', 'created_at')
    search_fields = ('text', 'translation', 'person_mentioned', 'context')
    ordering = ('-usage_count', '-created_at')
    readonly_fields = ('id', 'usage_count', 'created_at', 'updated_at')


@admin.register(PhrasePlayback)
class PhrasePlaybackAdmin(admin.ModelAdmin):
    list_display = ('phrase', 'user', 'played_at', 'duration_played')
    list_filter = ('played_at',)
    search_fields = ('phrase__text', 'user__name')
    ordering = ('-played_at',)
    readonly_fields = ('id', 'played_at',)


@admin.register(ConversationSession)
class ConversationSessionAdmin(admin.ModelAdmin):
    list_display = ('name', 'vault', 'phrases_count', 'total_playbacks', 'last_played', 'created_at')
    list_filter = ('auto_play', 'shuffle_order', 'vault', 'created_at')
    search_fields = ('name', 'description', 'vault__name')
    ordering = ('-last_played', '-created_at')
    readonly_fields = ('id', 'total_playbacks', 'last_played', 'created_at', 'updated_at')
    
    def phrases_count(self, obj):
        return obj.phrases.count()
    phrases_count.short_description = 'Frases'


@admin.register(ConversationPlayback)
class ConversationPlaybackAdmin(admin.ModelAdmin):
    list_display = ('session', 'user', 'started_at', 'ended_at', 'phrases_played')
    list_filter = ('started_at', 'ended_at')
    search_fields = ('session__name', 'user__name')
    ordering = ('-started_at',)
    readonly_fields = ('id', 'started_at',)
