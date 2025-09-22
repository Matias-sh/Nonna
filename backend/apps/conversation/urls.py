from django.urls import path
from . import views

urlpatterns = [
    path('phrases/', views.PhraseListCreateView.as_view(), name='phrase_list_create'),
    path('phrases/<uuid:pk>/', views.PhraseDetailView.as_view(), name='phrase_detail'),
    path('phrases/<uuid:phrase_id>/play/', views.play_phrase, name='play_phrase'),
    path('phrases/<uuid:phrase_id>/favorite/', views.toggle_favorite_phrase, name='toggle_favorite_phrase'),
    path('phrases/<uuid:phrase_id>/playbacks/', views.PhrasePlaybackListCreateView.as_view(), name='phrase_playback_list_create'),
    path('sessions/', views.ConversationSessionListCreateView.as_view(), name='conversation_session_list_create'),
    path('sessions/<uuid:pk>/', views.ConversationSessionDetailView.as_view(), name='conversation_session_detail'),
    path('sessions/<uuid:session_id>/playbacks/', views.ConversationPlaybackListCreateView.as_view(), name='conversation_playback_list_create'),
    path('vaults/<uuid:vault_id>/stats/', views.phrase_stats, name='phrase_stats'),
    path('vaults/<uuid:vault_id>/random/', views.random_phrases, name='random_phrases'),
]
