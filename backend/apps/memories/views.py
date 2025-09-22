from rest_framework import generics, status, permissions, filters
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser, FormParser
from django_filters.rest_framework import DjangoFilterBackend
from django.db.models import Q
from .models import Memory, MemoryComment, MemoryLike, MemoryShare
from .serializers import (
    MemorySerializer, MemoryCreateSerializer, MemoryUpdateSerializer,
    MemoryDetailSerializer, MemoryCommentSerializer, MemoryLikeSerializer,
    MemoryShareSerializer
)


class MemoryListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear recuerdos
    """
    permission_classes = [permissions.IsAuthenticated]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['type', 'vault', 'created_by']
    search_fields = ['title', 'description', 'tags']
    ordering_fields = ['created_at', 'updated_at', 'date_taken']
    ordering = ['-date_taken', '-created_at']
    parser_classes = [MultiPartParser, FormParser]
    
    def get_serializer_class(self):
        if self.request.method == 'POST':
            return MemoryCreateSerializer
        return MemorySerializer
    
    def get_queryset(self):
        user = self.request.user
        return Memory.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class MemoryDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar un recuerdo específico
    """
    permission_classes = [permissions.IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser]
    
    def get_serializer_class(self):
        if self.request.method in ['PUT', 'PATCH']:
            return MemoryUpdateSerializer
        return MemoryDetailSerializer
    
    def get_queryset(self):
        user = self.request.user
        return Memory.objects.filter(
            Q(vault__owner=user) | Q(vault__members__user=user)
        ).distinct()


class MemoryCommentListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear comentarios en un recuerdo
    """
    serializer_class = MemoryCommentSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        memory_id = self.kwargs['memory_id']
        return MemoryComment.objects.filter(memory_id=memory_id)
    
    def perform_create(self, serializer):
        memory_id = self.kwargs['memory_id']
        memory = Memory.objects.get(id=memory_id)
        serializer.save(memory=memory)


class MemoryCommentDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar un comentario específico
    """
    serializer_class = MemoryCommentSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        memory_id = self.kwargs['memory_id']
        return MemoryComment.objects.filter(memory_id=memory_id)


@api_view(['POST', 'DELETE'])
@permission_classes([permissions.IsAuthenticated])
def toggle_memory_like(request, memory_id):
    """
    Vista para dar/quitar like a un recuerdo
    """
    try:
        memory = Memory.objects.get(id=memory_id)
    except Memory.DoesNotExist:
        return Response(
            {'error': 'Recuerdo no encontrado'}, 
            status=status.HTTP_404_NOT_FOUND
        )
    
    if request.method == 'POST':
        like, created = MemoryLike.objects.get_or_create(
            memory=memory,
            user=request.user
        )
        if created:
            return Response(
                {'message': 'Like agregado'}, 
                status=status.HTTP_201_CREATED
            )
        else:
            return Response(
                {'message': 'Ya tienes like en este recuerdo'}, 
                status=status.HTTP_400_BAD_REQUEST
            )
    
    elif request.method == 'DELETE':
        try:
            like = MemoryLike.objects.get(memory=memory, user=request.user)
            like.delete()
            return Response(
                {'message': 'Like eliminado'}, 
                status=status.HTTP_200_OK
            )
        except MemoryLike.DoesNotExist:
            return Response(
                {'error': 'No tienes like en este recuerdo'}, 
                status=status.HTTP_404_NOT_FOUND
            )


class MemoryShareListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear compartidos de recuerdos
    """
    serializer_class = MemoryShareSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        return MemoryShare.objects.filter(
            Q(shared_by=user) | Q(shared_with=user)
        ).distinct()


class MemoryShareDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar un compartido específico
    """
    serializer_class = MemoryShareSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        return MemoryShare.objects.filter(
            Q(shared_by=user) | Q(shared_with=user)
        ).distinct()


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def memory_timeline(request):
    """
    Vista para obtener la línea de tiempo de recuerdos
    """
    user = request.user
    year = request.query_params.get('year')
    
    memories = Memory.objects.filter(
        Q(vault__owner=user) | Q(vault__members__user=user)
    ).distinct()
    
    if year:
        memories = memories.filter(date_taken__year=year)
    
    memories = memories.order_by('-date_taken', '-created_at')
    
    serializer = MemorySerializer(memories, many=True, context={'request': request})
    return Response(serializer.data)


@api_view(['GET'])
@permission_classes([permissions.IsAuthenticated])
def memory_stats(request):
    """
    Vista para obtener estadísticas de recuerdos
    """
    user = request.user
    memories = Memory.objects.filter(
        Q(vault__owner=user) | Q(vault__members__user=user)
    ).distinct()
    
    stats = {
        'total_memories': memories.count(),
        'by_type': {},
        'by_year': {},
        'total_likes': 0,
        'total_comments': 0,
    }
    
    # Estadísticas por tipo
    for memory_type, _ in Memory._meta.get_field('type').choices:
        count = memories.filter(type=memory_type).count()
        stats['by_type'][memory_type] = count
    
    # Estadísticas por año
    for memory in memories:
        if memory.date_taken:
            year = memory.date_taken.year
            stats['by_year'][year] = stats['by_year'].get(year, 0) + 1
        stats['total_likes'] += memory.likes.count()
        stats['total_comments'] += memory.comments.count()
    
    return Response(stats)


@api_view(['POST'])
@permission_classes([permissions.IsAuthenticated])
def upload_photo(request):
    """
    Vista para subir fotos
    """
    if 'photo' not in request.FILES:
        return Response(
            {'error': 'No se proporcionó archivo de foto'}, 
            status=status.HTTP_400_BAD_REQUEST
        )
    
    photo = request.FILES['photo']
    # Aquí se procesaría la foto y se guardaría
    # Por ahora, devolvemos una URL simulada
    photo_url = f"/media/memories/photos/{photo.name}"
    
    return Response(
        {'photo_url': photo_url}, 
        status=status.HTTP_201_CREATED
    )


@api_view(['POST'])
@permission_classes([permissions.IsAuthenticated])
def upload_audio(request):
    """
    Vista para subir archivos de audio
    """
    if 'audio' not in request.FILES:
        return Response(
            {'error': 'No se proporcionó archivo de audio'}, 
            status=status.HTTP_400_BAD_REQUEST
        )
    
    audio = request.FILES['audio']
    # Aquí se procesaría el audio y se guardaría
    # Por ahora, devolvemos una URL simulada
    audio_url = f"/media/memories/audio/{audio.name}"
    
    return Response(
        {'audio_url': audio_url}, 
        status=status.HTTP_201_CREATED
    )