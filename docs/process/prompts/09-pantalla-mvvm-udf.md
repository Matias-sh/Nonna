# Prompt 09 - Crear pantalla con MVVM + UDF

## Objetivo del prompt

Implementar una pantalla real con contrato profesional: `UiState`, `Event`, `ViewModel`, `Route`, `Screen`.

## Cuando usarlo

- Al crear/migrar una pantalla productiva.
- Cuando se necesite separar UI de negocio.

## Entradas requeridas

- Pantalla objetivo y feature.
- Template de arquitectura.
- Componentes DS disponibles.

## Salida esperada (archivos a crear/actualizar)

- Kotlin de feature (`Route`, `Screen`, `ViewModel`, `UiState`, `Event`).
- `docs/architecture/mvvm-udf.md`
- `docs/architecture/feature-template.md`
- `docs/features/[feature].md`
- Supuestos: `docs/design-system/11-technical-decisions.md`

## Checklist de calidad

- Screen pura y testeable.
- Route conecta ViewModel + lifecycle (`collectAsStateWithLifecycle`).
- Estados loading/error/empty/success segun caso.
- Previews por estados de `UiState`.

## Riesgos frecuentes

- Screen con side effects de negocio.
- Estado distribuido en varios lugares sin contrato.
- No documentar eventos de usuario.

## Plan de ejecucion paso a paso

1. Definir objetivo de pantalla y alcance.
2. Diseñar `UiState` y `Event`.
3. Implementar ViewModel y flujo de estado.
4. Crear Route y Screen separadas.
5. Agregar previews y actualizar doc de feature.

## Criterio de done

- Pantalla integrada con arquitectura canonica y docs actualizadas.

## Dependencias con prompts anteriores

- Requiere Prompt 02, 05, 08.
