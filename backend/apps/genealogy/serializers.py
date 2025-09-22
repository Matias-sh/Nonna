from rest_framework import serializers
from .models import Person, Relation, PersonMemory
from apps.accounts.serializers import UserSerializer


class PersonSerializer(serializers.ModelSerializer):
    full_name = serializers.ReadOnlyField()
    age = serializers.ReadOnlyField()
    created_by_name = serializers.CharField(source='created_by.name', read_only=True)
    vault_name = serializers.CharField(source='vault.name', read_only=True)
    memories_count = serializers.SerializerMethodField()
    
    class Meta:
        model = Person
        fields = (
            'id', 'first_name', 'last_name', 'middle_name', 'full_name',
            'birth_date', 'death_date', 'birth_place', 'death_place',
            'email', 'phone', 'address', 'photo', 'documents',
            'occupation', 'notes', 'is_living', 'age',
            'vault', 'vault_name', 'created_by', 'created_by_name',
            'memories_count', 'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_by', 'created_at', 'updated_at')
    
    def get_memories_count(self, obj):
        return obj.memories.count()
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class PersonCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Person
        fields = (
            'first_name', 'last_name', 'middle_name', 'birth_date', 'death_date',
            'birth_place', 'death_place', 'email', 'phone', 'address', 'photo',
            'documents', 'occupation', 'notes', 'is_living', 'vault'
        )
    
    def create(self, validated_data):
        validated_data['created_by'] = self.context['request'].user
        return super().create(validated_data)


class PersonUpdateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Person
        fields = (
            'first_name', 'last_name', 'middle_name', 'birth_date', 'death_date',
            'birth_place', 'death_place', 'email', 'phone', 'address', 'photo',
            'documents', 'occupation', 'notes', 'is_living'
        )


class RelationSerializer(serializers.ModelSerializer):
    person1_name = serializers.CharField(source='person1.full_name', read_only=True)
    person2_name = serializers.CharField(source='person2.full_name', read_only=True)
    
    class Meta:
        model = Relation
        fields = (
            'id', 'person1', 'person1_name', 'person2', 'person2_name',
            'relation_type', 'start_date', 'end_date', 'notes',
            'created_at', 'updated_at'
        )
        read_only_fields = ('id', 'created_at', 'updated_at')


class RelationCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = Relation
        fields = ('person1', 'person2', 'relation_type', 'start_date', 'end_date', 'notes')


class PersonMemorySerializer(serializers.ModelSerializer):
    memory_title = serializers.CharField(source='memory.title', read_only=True)
    memory_type = serializers.CharField(source='memory.type', read_only=True)
    memory_photo = serializers.ImageField(source='memory.photo', read_only=True)
    
    class Meta:
        model = PersonMemory
        fields = (
            'id', 'person', 'memory', 'memory_title', 'memory_type',
            'memory_photo', 'role', 'created_at'
        )
        read_only_fields = ('id', 'created_at')


class PersonDetailSerializer(PersonSerializer):
    relations_from = RelationSerializer(many=True, read_only=True)
    relations_to = RelationSerializer(many=True, read_only=True)
    memories = PersonMemorySerializer(many=True, read_only=True)
    
    class Meta(PersonSerializer.Meta):
        fields = PersonSerializer.Meta.fields + ('relations_from', 'relations_to', 'memories')


class GenealogyGraphSerializer(serializers.Serializer):
    """
    Serializer para el grafo completo del árbol genealógico
    """
    persons = PersonSerializer(many=True)
    relations = RelationSerializer(many=True)
    
    def to_representation(self, instance):
        return {
            'persons': PersonSerializer(instance['persons'], many=True, context=self.context).data,
            'relations': RelationSerializer(instance['relations'], many=True, context=self.context).data,
        }
