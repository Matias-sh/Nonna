# Prompt 16 - Crear pantalla nueva profesionalmente

## Objetivo del prompt

Crear una nueva pantalla con proceso estandar DS + MVVM/UDF antes de implementar.

## Cuando usarlo

- Cada vez que se suma una pantalla nueva.

## Entradas requeridas

- Nombre de pantalla y feature.
- Objetivo funcional.
- Reglas de negocio y estados esperados.

## Salida esperada (archivos a crear/actualizar)

- Propuesta de:
  - `[Feature]Route.kt`
  - `[Feature]Screen.kt`
  - `[Feature]ViewModel.kt`
  - `[Feature]UiState.kt`
  - `[Feature]Event.kt`
- `docs/features/[feature].md`

## Checklist de calidad

- Analisis de estado y eventos.
- Reuso de componentes DS existentes.
- Riesgos de accesibilidad y performance.
- Previews y tests planificados.

## Riesgos frecuentes

- Saltarse fase de analisis y copiar pantalla.
- Mezclar ViewModel en composables.
- Crear componentes nuevos sin evaluar reuso.

## Plan de ejecucion paso a paso

1. Analizar objetivo y estados de pantalla.
2. Definir `UiState` y `Event`.
3. Diseñar Route/Screen.
4. Identificar componentes a reutilizar/crear.
5. Documentar plan y pedir aprobacion.

## Criterio de done

- Plan aprobado con archivos, flujo y calidad definidos antes de codificar.

## Dependencias con prompts anteriores

- Requiere 02, 05, 08, 09.
