from django.urls import path
from . import views

urlpatterns = [
    path('persons/', views.PersonListCreateView.as_view(), name='person_list_create'),
    path('persons/<uuid:pk>/', views.PersonDetailView.as_view(), name='person_detail'),
    path('persons/<uuid:person_id>/memories/', views.PersonMemoryListCreateView.as_view(), name='person_memory_list_create'),
    path('persons/<uuid:person_id>/memories/<uuid:pk>/', views.PersonMemoryDetailView.as_view(), name='person_memory_detail'),
    path('persons/<uuid:person_id>/family-tree/', views.person_family_tree, name='person_family_tree'),
    path('relations/', views.RelationListCreateView.as_view(), name='relation_list_create'),
    path('relations/<uuid:pk>/', views.RelationDetailView.as_view(), name='relation_detail'),
    path('vaults/<uuid:vault_id>/graph/', views.genealogy_graph, name='genealogy_graph'),
    path('vaults/<uuid:vault_id>/stats/', views.genealogy_stats, name='genealogy_stats'),
]
