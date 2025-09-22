from rest_framework import generics, status, permissions, filters
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q
from .models import Person, Relation, PersonMemory
from .serializers import (
    PersonSerializer, PersonCreateSerializer, PersonUpdateSerializer,
    PersonDetailSerializer, RelationSerializer, RelationCreateSerializer,
    PersonMemorySerializer, GenealogyGraphSerializer
)


class PersonListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear personas
    """
    permission_classes = [permissions.IsAuthenticated]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['vault', 'is_living']
    search_fields = ['first_name', 'last_name', 'middle_name', 'occupation']
    ordering_fields = ['first_name', 'last_name', 'birth_date', 'created_at']
    ordering = ['last_name', 'first_name']
    
    def get_serializer_class(self):
        if self.request.method == 'POST':
            return PersonCreateSerializer
        return PersonSerializer
    
    def get_queryset(self):
        user = self.request.user
        return Person.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class PersonDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar una persona específica
    """
    permission_classes = [permissions.IsAuthenticated]
    
    def get_serializer_class(self):
        if self.request.method in ['PUT', 'PATCH']:
            return PersonUpdateSerializer
        return PersonDetailSerializer
    
    def get_queryset(self):
        user = self.request.user
        return Person.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class RelationListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear relaciones
    """
    permission_classes = [permissions.IsAuthenticated]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter]
    filterset_fields = ['relation_type', 'person1', 'person2']
    
    def get_serializer_class(self):
        if self.request.method == 'POST':
            return RelationCreateSerializer
        return RelationSerializer
    
    def get_queryset(self):
        user = self.request.user
        return Relation.objects.filter(
            Q(person1__vault__owner=user) | Q(person1__vault__members__user=user) |
            Q(person2__vault__owner=user) | Q(person2__vault__members__user=user)
        ).distinct()


class RelationDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar una relación específica
    """
    serializer_class = RelationSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        return Relation.objects.filter(
            Q(person1__vault__owner=user) | Q(person1__vault__members__user=user) |
            Q(person2__vault__owner=user) | Q(person2__vault__members__user=user)
        ).distinct()


class PersonMemoryListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear asociaciones persona-recuerdo
    """
    serializer_class = PersonMemorySerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        person_id = self.kwargs['person_id']
        return PersonMemory.objects.filter(person_id=person_id)
    
    def perform_create(self, serializer):
        person_id = self.kwargs['person_id']
        person = Person.objects.get(id=person_id)
        serializer.save(person=person)


class PersonMemoryDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar una asociación específica
    """
    serializer_class = PersonMemorySerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        person_id = self.kwargs['person_id']
        return PersonMemory.objects.filter(person_id=person_id)


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def genealogy_graph(request, vault_id):
    """
    Vista para obtener el grafo completo del árbol genealógico
    """
    user = request.user
    
    # Verificar que el usuario tiene acceso al vault
    from apps.accounts.models import Vault
    vault = Vault.objects.filter(
        id=vault_id
    ).filter(
        Q(owner=user) | Q(members__user=user)
    ).first()
    
    if not vault:
        return Response(
            {'error': 'Vault no encontrado o sin permisos'}, 
            status=status.HTTP_404_NOT_FOUND
        )
    
    # Obtener todas las personas y relaciones del vault
    persons = Person.objects.filter(vault=vault)
    relations = Relation.objects.filter(
        Q(person1__vault=vault) | Q(person2__vault=vault)
    ).distinct()
    
    data = {
        'persons': persons,
        'relations': relations
    }
    
    serializer = GenealogyGraphSerializer(data, context={'request': request})
    return Response(serializer.data)


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def person_family_tree(request, person_id):
    """
    Vista para obtener el árbol familiar de una persona específica
    """
    user = self.request.user
    
    person = Person.objects.filter(
        id=person_id
    ).filter(
        Q(vault__owner=user) | Q(vault__members__user=user)
    ).first()
    
    if not person:
        return Response(
            {'error': 'Persona no encontrada'}, 
            status=status.HTTP_404_NOT_FOUND
        )
    
    # Obtener familiares directos
    family_relations = Relation.objects.filter(
        Q(person1=person) | Q(person2=person)
    )
    
    family_members = []
    for relation in family_relations:
        if relation.person1 == person:
            family_members.append(relation.person2)
        else:
            family_members.append(relation.person1)
    
    # Serializar datos
    person_serializer = PersonDetailSerializer(person, context={'request': request})
    family_serializer = PersonSerializer(family_members, many=True, context={'request': request})
    relations_serializer = RelationSerializer(family_relations, many=True, context={'request': request})
    
    return Response({
        'person': person_serializer.data,
        'family_members': family_serializer.data,
        'relations': relations_serializer.data
    })


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def genealogy_stats(request, vault_id):
    """
    Vista para obtener estadísticas del árbol genealógico
    """
    user = request.user
    
    # Verificar acceso al vault
    from apps.accounts.models import Vault
    vault = Vault.objects.filter(
        id=vault_id
    ).filter(
        Q(owner=user) | Q(members__user=user)
    ).first()
    
    if not vault:
        return Response(
            {'error': 'Vault no encontrado'}, 
            status=status.HTTP_404_NOT_FOUND
        )
    
    persons = Person.objects.filter(vault=vault)
    relations = Relation.objects.filter(
        Q(person1__vault=vault) | Q(person2__vault=vault)
    ).distinct()
    
    stats = {
        'total_persons': persons.count(),
        'living_persons': persons.filter(is_living=True).count(),
        'deceased_persons': persons.filter(is_living=False).count(),
        'total_relations': relations.count(),
        'by_relation_type': {},
        'by_generation': {},
    }
    
    # Estadísticas por tipo de relación
    for relation_type, _ in Relation._meta.get_field('relation_type').choices:
        count = relations.filter(relation_type=relation_type).count()
        stats['by_relation_type'][relation_type] = count
    
    # Estadísticas por generación (aproximado por edad)
    for person in persons:
        if person.age is not None:
            if person.age < 18:
                generation = 'children'
            elif person.age < 40:
                generation = 'adults'
            elif person.age < 65:
                generation = 'middle_aged'
            else:
                generation = 'seniors'
            
            stats['by_generation'][generation] = stats['by_generation'].get(generation, 0) + 1
    
    return Response(stats)
