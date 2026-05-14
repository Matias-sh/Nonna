# App Architecture - Nonna

## Stack

- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM + UDF
- Hilt DI

## Capas macro

- `data`: fuentes remotas/locales, DTOs, repositorios.
- `domain` (si aplica): reglas de negocio y casos de uso.
- `ui`: presentation Compose (design system + features).

## Principios de separacion

- UI no llama repositorios/APIs directamente.
- ViewModel orquesta casos de uso/repositorios.
- Composables se enfocan en render + intenciones del usuario.

## Objetivo de evolucion

Migrar progresivamente hacia una organizacion por feature con contrato presentation uniforme:

- `Route`
- `Screen`
- `UiState`
- `UiEvent`
- `ViewModel`
