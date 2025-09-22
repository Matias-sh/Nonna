from rest_framework import serializers
from .models import Phrase, PhrasePlayback, ConversationSession, ConversationPlayback
from apps.accounts.serializers import UserSerializer


class PhraseSerializer(serializers.ModelSerializer):
    created_by_name = serializers.CharField(source='created_by.name', read_only=True)
    vault_name = serializers.CharField(source='vault.name', read_only=True)
    playbacks_count = serializers.SerializerMethodField()
    
    class Meta:
        model = Phrase
        fields = (
            'id', 'text', 'translation', 'category', 'context', 'person_mentioned',
            'language', 'audio_file', 'audio_duration', 'tags', 'is_favorite',
            'usage_count', 'playbacks_count', 'vault', 'vault_name',
            'created_by', 'created_by_name', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_by', 'usage_count', 'created_at', 'updated_at')
    
    def get_playbacks_count(self, obj):
        return obj.playbacks.count()
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class PhraseCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Phrase
        fields = (
            'text', 'translation', 'category', 'context', 'person_mentioned',
            'language', 'audio_file', 'tags', 'is_favorite', 'vault'
        )
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class PhraseUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Phrase
        fields = (
            'text', 'translation', 'category', 'context', 'person_mentioned',
            'language', 'audio_file', 'tags', 'is_favorite'
        )


class PhrasePlaybackSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)
    phrase_text = serializers.CharField(source='phrase.text', read_only=True)
    
    class Meta:
        model = PhrasePlayback
        fields = ('id', 'phrase', 'phrase_text', 'user', 'user_name', 'played_at', 'duration_played')
        read_only_fields = ('id', 'user', 'played_at')
    
    def create(self, validated_data):
        validated_data['user'] = self.context['request'].user
        return super().create(validated_data)


class ConversationSessionSerializer(serializers.ModelSerializer):
    created_by_name = serializers.CharField(source='created_by.name', read_only=True)
    vault_name = serializers.CharField(source='vault.name', read_only=True)
    phrases_count = serializers.SerializerMethodField()
    
    class Meta:
        model = ConversationSession
        fields = (
            'id', 'name', 'description', 'phrases', 'auto_play', 'shuffle_order',
            'total_playbacks', 'last_played', 'phrases_count', 'vault', 'vault_name',
            'created_by', 'created_by_name', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_by', 'total_playbacks', 'last_played', 'created_at', 'updated_at')
    
    def get_phrases_count(self, obj):
        return obj.phrases.count()
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class ConversationSessionCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = ConversationSession
        fields = ('name', 'description', 'phrases', 'auto_play', 'shuffle_order', 'vault')
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class ConversationSessionDetailSerializer(ConversationSessionSerializer):
    phrases = PhraseSerializer(many=True, read_only=True)
    
    class Meta(ConversationSessionSerializer.Meta):
        fields = ConversationSessionSerializer.Meta.fields


class ConversationPlaybackSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)
    session_name = serializers.CharField(source='session.name', read_only=True)
    
    class Meta:
        model = ConversationPlayback
        fields = (
            'id', 'session', 'session_name', 'user', 'user_name',
            'started_at', 'ended_at', 'phrases_played'
        )
        read_only_fields = ('id', 'user', 'started_at')
    
    def create(self, validated_data):
        validated_data['user'] = self.context['request'].user
        return super().create(validated_data)


class PhraseStatsSerializer(serializers.Serializer):
    """
    Serializer para estadísticas de frases
    """
    total_phrases = serializers.IntegerField()
    by_category = serializers.DictField()
    most_used = serializers.ListField()
    recent_playbacks = serializers.ListField()
    total_playbacks = serializers.IntegerField()
