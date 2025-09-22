from rest_framework import serializers
from .models import Memory, MemoryComment, MemoryLike, MemoryShare
from apps.accounts.serializers import UserSerializer


class MemorySerializer(serializers.ModelSerializer):
    created_by_name = serializers.CharField(source='created_by.name', read_only=True)
    vault_name = serializers.CharField(source='vault.name', read_only=True)
    likes_count = serializers.SerializerMethodField()
    comments_count = serializers.SerializerMethodField()
    is_liked = serializers.SerializerMethodField()
    
    class Meta:
        model = Memory
        fields = (
            'id', 'title', 'description', 'type', 'photo', 'audio', 'video',
            'date_taken', 'location', 'tags', 'vault', 'vault_name',
            'created_by', 'created_by_name', 'likes_count', 'comments_count',
            'is_liked', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_by', 'created_at', 'updated_at')
    
    def get_likes_count(self, obj):
        return obj.likes.count()
    
    def get_comments_count(self, obj):
        return obj.comments.count()
    
    def get_is_liked(self, obj):
        request = self.context.get('request')
        if request and request.user.is_authenticated:
            return obj.likes.filter(user=request.user).exists()
        return False
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class MemoryCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Memory
        fields = (
            'title', 'description', 'type', 'photo', 'audio', 'video',
            'date_taken', 'location', 'tags', 'vault'
        )
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class MemoryUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Memory
        fields = (
            'title', 'description', 'type', 'photo', 'audio', 'video',
            'date_taken', 'location', 'tags'
        )


class MemoryCommentSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)
    user_avatar = serializers.ImageField(source='user.avatar', read_only=True)
    
    class Meta:
        model = MemoryComment
        fields = ('id', 'text', 'user', 'user_name', 'user_avatar', 'created_at', 'updated_at')
        read_only_fields = ('id', 'user', 'created_at', 'updated_at')
    
    def create(self, validated_data):
        validated_data['user'] = self.context['request'].user
        return super().create(validated_data)


class MemoryLikeSerializer(serializers.ModelSerializer):
    user_name = serializers.CharField(source='user.name', read_only=True)
    
    class Meta:
        model = MemoryLike
        fields = ('user', 'user_name', 'created_at')
        read_only_fields = ('user', 'created_at')
    
    def create(self, validated_data):
        validated_data['user'] = self.context['request'].user
        return super().create(validated_data)


class MemoryShareSerializer(serializers.ModelSerializer):
    shared_by_name = serializers.CharField(source='shared_by.name', read_only=True)
    shared_with_name = serializers.CharField(source='shared_with.name', read_only=True)
    memory_title = serializers.CharField(source='memory.title', read_only=True)
    
    class Meta:
        model = MemoryShare
        fields = (
            'id', 'memory', 'memory_title', 'shared_by', 'shared_by_name',
            'shared_with', 'shared_with_name', 'message', 'created_at'
        )
        read_only_fields = ('id', 'shared_by', 'created_at')
    
    def create(self, validated_data):
        validated_data['shared_by'] = self.context['request'].user
        return super().create(validated_data)


class MemoryDetailSerializer(MemorySerializer):
    comments = MemoryCommentSerializer(many=True, read_only=True)
    likes = MemoryLikeSerializer(many=True, read_only=True)
    
    class Meta(MemorySerializer.Meta):
        fields = MemorySerializer.Meta.fields + ('comments', 'likes')
