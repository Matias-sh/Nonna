from rest_framework import generics, status, permissions, filters
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q, Count
from .models import Phrase, PhrasePlayback, ConversationSession, ConversationPlayback
from .serializers import (
    PhraseSerializer, PhraseCreateSerializer, PhraseUpdateSerializer,
    PhrasePlaybackSerializer, ConversationSessionSerializer,
    ConversationSessionCreateSerializer, ConversationSessionDetailSerializer,
    ConversationPlaybackSerializer, PhraseStatsSerializer
)


class PhraseListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear frases
    """
    permission_classes = [permissions.IsAuthenticated]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['category', 'vault', 'is_favorite']
    search_fields = ['text', 'translation', 'person_mentioned', 'context']
    ordering_fields = ['usage_count', 'created_at', 'text']
    ordering = ['-usage_count', '-created_at']
    
    def get_serializer_class(self):
        if self.request.method == 'POST':
            return PhraseCreateSerializer
        return PhraseSerializer
    
    def get_queryset(self):
        user = self.request.user
        return Phrase.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class PhraseDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar una frase específica
    """
    permission_classes = [permissions.IsAuthenticated]
    
    def get_serializer_class(self):
        if self.request.method in ['PUT', 'PATCH']:
            return PhraseUpdateSerializer
        return PhraseSerializer
    
    def get_queryset(self):
        user = self.request.user
        return Phrase.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class PhrasePlaybackListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear reproducciones de frases
    """
    serializer_class = PhrasePlaybackSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        phrase_id = self.kwargs['phrase_id']
        return PhrasePlayback.objects.filter(phrase_id=phrase_id)
    
    def perform_create(self, serializer):
        phrase_id = self.kwargs['phrase_id']
        phrase = Phrase.objects.get(id=phrase_id)
        serializer.save(phrase=phrase)
        
        # Incrementar contador de uso
        phrase.usage_count += 1
        phrase.save()


class ConversationSessionListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear sesiones de conversación
    """
    permission_classes = [permissions.IsAuthenticated]
    
    def get_serializer_class(self):
        if self.request.method == 'POST':
            return ConversationSessionCreateSerializer
        return ConversationSessionSerializer
    
    def get_queryset(self):
        user = self.request.user
        return ConversationSession.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class ConversationSessionDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar una sesión específica
    """
    permission_classes = [permissions.IsAuthenticated]
    
    def get_serializer_class(self):
        if self.request.method == 'GET':
            return ConversationSessionDetailSerializer
        return ConversationSessionSerializer
    
    def get_queryset(self):
        user = self.request.user
        return ConversationSession.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class ConversationPlaybackListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear reproducciones de sesiones
    """
    serializer_class = ConversationPlaybackSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        session_id = self.kwargs['session_id']
        return ConversationPlayback.objects.filter(session_id=session_id)
    
    def perform_create(self, serializer):
        session_id = self.kwargs['session_id']
        session = ConversationSession.objects.get(id=session_id)
        serializer.save(session=session)


@api_view(['POST'])
@permission_classes([permissions.IsAuthenticated])
def play_phrase(request, phrase_id):
    """
    Vista para reproducir una frase y registrar la reproducción
    """
    try:
        phrase = Phrase.objects.get(id=phrase_id)
    except Phrase.DoesNotExist:
        return Response(
            {'error': 'Frase no encontrada'}, 
            status=status.HTTP_404_NOT_FOUND
        )
    
    # Crear registro de reproducción
    playback = PhrasePlayback.objects.create(
        phrase=phrase,
        user=request.user
    )
    
    # Incrementar contador de uso
    phrase.usage_count += 1
    phrase.save()
    
    serializer = PhrasePlaybackSerializer(playback, context={'request': request})
    return Response(serializer.data, status=status.HTTP_201_CREATED)


@api_view(['POST'])
@permission_classes([permissions.IsAuthenticated])
def toggle_favorite_phrase(request, phrase_id):
    """
    Vista para marcar/desmarcar una frase como favorita
    """
    try:
        phrase = Phrase.objects.get(id=phrase_id)
    except Phrase.DoesNotExist:
        return Response(
            {'error': 'Frase no encontrada'}, 
            status=status.HTTP_404_NOT_FOUND
        )
    
    phrase.is_favorite = not phrase.is_favorite
    phrase.save()
    
    serializer = PhraseSerializer(phrase, context={'request': request})
    return Response(serializer.data)


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def phrase_stats(request, vault_id):
    """
    Vista para obtener estadísticas de frases
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
    
    phrases = Phrase.objects.filter(vault=vault)
    
    # Estadísticas por categoría
    by_category = {}
    for category, _ in Phrase._meta.get_field('category').choices:
        count = phrases.filter(category=category).count()
        by_category[category] = count
    
    # Frases más usadas
    most_used = phrases.order_by('-usage_count')[:5]
    most_used_data = PhraseSerializer(most_used, many=True, context={'request': request}).data
    
    # Reproducciones recientes
    recent_playbacks = PhrasePlayback.objects.filter(
        phrase__vault=vault
    ).order_by('-played_at')[:10]
    recent_playbacks_data = PhrasePlaybackSerializer(
        recent_playbacks, many=True, context={'request': request}
    ).data
    
    # Total de reproducciones
    total_playbacks = PhrasePlayback.objects.filter(phrase__vault=vault).count()
    
    stats = {
        'total_phrases': phrases.count(),
        'by_category': by_category,
        'most_used': most_used_data,
        'recent_playbacks': recent_playbacks_data,
        'total_playbacks': total_playbacks,
    }
    
    serializer = PhraseStatsSerializer(stats)
    return Response(serializer.data)


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def random_phrases(request, vault_id):
    """
    Vista para obtener frases aleatorias
    """
    user = request.user
    count = int(request.query_params.get('count', 5))
    
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
    
    phrases = Phrase.objects.filter(vault=vault).order_by('?')[:count]
    serializer = PhraseSerializer(phrases, many=True, context={'request': request})
    return Response(serializer.data)
