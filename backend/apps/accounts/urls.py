from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import views

urlpatterns = [
    path('register/', views.RegisterView.as_view(), name='register'),
    path('login/', views.LoginView.as_view(), name='login'),
    path('refresh/', views.refresh_token, name='refresh_token'),
    path('profile/', views.UserProfileView.as_view(), name='user_profile'),
    path('update/', views.UserUpdateView.as_view(), name='user_update'),
    path('vaults/', views.VaultListCreateView.as_view(), name='vault_list_create'),
    path('vaults/<uuid:vault_id>/', views.VaultDetailView.as_view(), name='vault_detail'),
    path('vaults/<uuid:vault_id>/members/', views.VaultMemberListCreateView.as_view(), name='vault_member_list_create'),
    path('vaults/<uuid:vault_id>/members/<int:pk>/', views.VaultMemberDetailView.as_view(), name='vault_member_detail'),
]
