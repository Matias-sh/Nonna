# Tareas Pendientes - Nonna App

## 🚧 Implementaciones Pendientes

### 1. Subida de Archivos al Backend (CRÍTICO)
**Estado:** Pendiente
**Prioridad:** Alta
**Descripción:** Implementar la subida de archivos (fotos y audios) al servidor cuando se crea un recuerdo.

**Detalles técnicos:**
- Actualmente `CreateMemoryViewModel.saveMemory()` crea el recuerdo con rutas locales
- Necesita:
  1. Subir la foto al endpoint `/api/memories/{id}/upload_photo/` usando multipart/form-data
  2. Subir el audio al endpoint `/api/memories/{id}/upload_audio/` usando multipart/form-data
  3. Obtener las URLs remotas del servidor
  4. Crear el recuerdo en el backend con las URLs remotas
  5. Eliminar archivos locales después de subida exitosa

**Archivos afectados:**
- `app/src/main/java/com/cocido/nonna/ui/memories/CreateMemoryViewModel.kt`
- `app/src/main/java/com/cocido/nonna/data/remote/api/MemoryApiService.kt`
- `app/src/main/java/com/cocido/nonna/data/repository/MemoryRepositoryImpl.kt`

**Endpoints del backend:**
```
POST /api/memories/              # Crear recuerdo
POST /api/memories/{id}/upload_photo/  # Subir foto
POST /api/memories/{id}/upload_audio/  # Subir audio
```

---

### 2. Sincronización de Baúles Creados Localmente
**Estado:** Completado ✅
**Descripción:** Los baúles ahora se crean directamente en el backend, no hay baúles "fantasma" locales.

---

### 3. Eliminación de Datos Dummy/Hardcodeados
**Estado:** Completado ✅
**Descripción:** Se eliminaron todos los recuerdos dummy. La app solo muestra datos reales del backend.

---

### 4. Manejo de Transcripciones de Audio (OPCIONAL)
**Estado:** Pendiente
**Prioridad:** Media
**Descripción:** Implementar transcripción automática de audios usando servicios de speech-to-text.

**Consideraciones:**
- Podría usar Google Cloud Speech-to-Text API
- O implementar Whisper de OpenAI en el backend
- Almacenar transcripciones en el campo `transcript` del modelo Memory

---

### 5. Gestión de Personas (TODO en código)
**Estado:** Pendiente
**Prioridad:** Media
**Descripción:** Implementar la funcionalidad completa de personas (members) en los recuerdos.

**Archivos con TODO:**
- `app/src/main/java/com/cocido/nonna/ui/memories/MemoryDetailFragment.kt:160` - Cargar nombre de la persona desde el repositorio
- `app/src/main/java/com/cocido/nonna/ui/memories/MemoryDetailFragment.kt:165` - Navegar al perfil de la persona

---

### 6. Timeline y Búsqueda de Recuerdos (TODO en código)
**Estado:** Pendiente
**Prioridad:** Baja
**Descripción:** Implementar funcionalidades de línea de tiempo y búsqueda.

**Archivos con TODO:**
- `app/src/main/java/com/cocido/nonna/data/repository/MemoryRepositoryImpl.kt:94` - Implementar searchMemories
- `app/src/main/java/com/cocido/nonna/data/repository/MemoryRepositoryImpl.kt:118` - Implementar getAllMemories

---

### 7. Gestión de Frases/Dichos (TODO en código)
**Estado:** Pendiente
**Prioridad:** Baja
**Descripción:** Implementar funcionalidad completa de frases y dichos familiares.

**Archivos con TODO:**
- `app/src/main/java/com/cocido/nonna/data/repository/MemoryRepositoryImpl.kt:109-119` - Implementar CRUD de frases

---

## 🐛 Bugs Conocidos

### 1. Layout fragment_create_vault.xml
**Estado:** Corregido ✅
**Descripción:** Error "ScrollView can host only one direct child" - Se cambió a ConstraintLayout con ScrollView interno.

---

## 🔧 Mejoras Técnicas Recomendadas

### 1. Migración de Kapt a KSP
**Prioridad:** Media
**Descripción:** Moshi actualmente usa Kapt (deprecado). Migrar a KSP.

### 2. Manejo de Errores Mejorado
**Prioridad:** Media
**Descripción:** Implementar manejo de errores más robusto con retry logic y mensajes específicos al usuario.

### 3. Offline-First Architecture
**Prioridad:** Alta
**Descripción:** Mejorar la arquitectura offline-first:
- Queue de sincronización para operaciones offline
- Work Manager para sincronización en background
- Indicadores visuales de estado de sincronización

---

## 📝 Notas

- La app está configurada como SaaS: todos los datos se guardan en el backend
- La base de datos local (Room) se usa solo como caché temporal
- No hay datos hardcodeados en producción
- El flujo de creación de baúles funciona correctamente con el backend

**Última actualización:** 2025-10-03
**Versión de la app:** Free Debug Build
