from rest_framework import status, generics, permissions
from rest_framework.decorators import api_view, permission_classes
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth import get_user_model
from .models import Vault, VaultMember
from .serializers import (
    UserRegistrationSerializer, UserLoginSerializer, UserSerializer,
    UserUpdateSerializer, VaultSerializer, VaultDetailSerializer,
    VaultMemberSerializer
)

User = get_user_model()


class RegisterView(APIView):
    """
    Vista para registro de nuevos usuarios
    """
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        serializer = UserRegistrationSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            refresh = RefreshToken.for_user(user)
            return Response({
                'access_token': str(refresh.access_token),
                'refresh_token': str(refresh),
                'user': UserSerializer(user).data
            }, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class LoginView(APIView):
    """
    Vista para login de usuarios
    """
    permission_classes = [permissions.AllowAny]
    
    def post(self, request):
        serializer = UserLoginSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.validated_data['user']
            refresh = RefreshToken.for_user(user)
            return Response({
                'access_token': str(refresh.access_token),
                'refresh_token': str(refresh),
                'user': UserSerializer(user).data
            }, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class UserProfileView(generics.RetrieveUpdateAPIView):
    """
    Vista para obtener y actualizar perfil de usuario
    """
    serializer_class = UserSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_object(self):
        return self.request.user


class UserUpdateView(generics.UpdateAPIView):
    """
    Vista para actualizar información del usuario
    """
    serializer_class = UserUpdateSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_object(self):
        return self.request.user


class VaultListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y crear cofres
    """
    serializer_class = VaultSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        return Vault.objects.filter(
            models.Q(owner=user) | models.Q(members__user=user)
        ).distinct()


class VaultDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar un cofre específico
    """
    serializer_class = VaultDetailSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        user = self.request.user
        return Vault.objects.filter(
            models.Q(owner=user) | models.Q(members__user=user)
        ).distinct()


class VaultMemberListCreateView(generics.ListCreateAPIView):
    """
    Vista para listar y agregar miembros a un cofre
    """
    serializer_class = VaultMemberSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        vault_id = self.kwargs['vault_id']
        return VaultMember.objects.filter(vault_id=vault_id)
    
    def perform_create(self, serializer):
        vault_id = self.kwargs['vault_id']
        vault = Vault.objects.get(id=vault_id)
        serializer.save(vault=vault)


class VaultMemberDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    Vista para obtener, actualizar y eliminar un miembro específico
    """
    serializer_class = VaultMemberSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        vault_id = self.kwargs['vault_id']
        return VaultMember.objects.filter(vault_id=vault_id)


@api_view(['POST'])
@permission_classes([permissions.IsAuthenticated])
def refresh_token(request):
    """
    Vista para refrescar token JWT
    """
    refresh_token = request.data.get('refresh_token')
    if not refresh_token:
        return Response(
            {'error': 'refresh_token es requerido'}, 
            status=status.HTTP_400_BAD_REQUEST
        )
    
    try:
        refresh = RefreshToken(refresh_token)
        return Response({
            'access_token': str(refresh.access_token),
        }, status=status.HTTP_200_OK)
    except Exception as e:
        return Response(
            {'error': 'Token inválido'}, 
            status=status.HTTP_400_BAD_REQUEST
        )
