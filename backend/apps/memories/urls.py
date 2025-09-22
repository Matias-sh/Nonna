from django.urls import path
from . import views

urlpatterns = [
    path('', views.MemoryListCreateView.as_view(), name='memory_list_create'),
    path('<uuid:pk>/', views.MemoryDetailView.as_view(), name='memory_detail'),
    path('<uuid:memory_id>/comments/', views.MemoryCommentListCreateView.as_view(), name='memory_comment_list_create'),
    path('<uuid:memory_id>/comments/<uuid:pk>/', views.MemoryCommentDetailView.as_view(), name='memory_comment_detail'),
    path('<uuid:memory_id>/like/', views.toggle_memory_like, name='toggle_memory_like'),
    path('shares/', views.MemoryShareListCreateView.as_view(), name='memory_share_list_create'),
    path('shares/<uuid:pk>/', views.MemoryShareDetailView.as_view(), name='memory_share_detail'),
    path('timeline/', views.memory_timeline, name='memory_timeline'),
    path('stats/', views.memory_stats, name='memory_stats'),
    path('uploads/photo', views.upload_photo, name='upload_photo'),
    path('uploads/audio', views.upload_audio, name='upload_audio'),
]
