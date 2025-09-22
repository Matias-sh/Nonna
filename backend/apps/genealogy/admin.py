from django.contrib import admin
from .models import Person, Relation, PersonMemory


@admin.register(Person)
class PersonAdmin(admin.ModelAdmin):
    list_display = ('full_name', 'birth_date', 'is_living', 'vault', 'created_by', 'created_at')
    list_filter = ('is_living', 'vault', 'created_at', 'birth_date')
    search_fields = ('first_name', 'last_name', 'middle_name', 'occupation', 'vault__name')
    ordering = ('last_name', 'first_name')
    readonly_fields = ('id', 'full_name', 'age', 'created_at', 'updated_at')


@admin.register(Relation)
class RelationAdmin(admin.ModelAdmin):
    list_display = ('person1', 'relation_type', 'person2', 'start_date', 'created_at')
    list_filter = ('relation_type', 'start_date', 'created_at')
    search_fields = ('person1__first_name', 'person1__last_name', 'person2__first_name', 'person2__last_name')
    ordering = ('-created_at',)
    readonly_fields = ('id', 'created_at', 'updated_at')


@admin.register(PersonMemory)
class PersonMemoryAdmin(admin.ModelAdmin):
    list_display = ('person', 'memory', 'role', 'created_at')
    list_filter = ('role', 'created_at')
    search_fields = ('person__first_name', 'person__last_name', 'memory__title')
    ordering = ('-created_at',)
    readonly_fields = ('id', 'created_at',)
