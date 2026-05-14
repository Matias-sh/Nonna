# Prompt 05 - Implementar componentes base

## Objetivo del prompt

Implementar componentes base del DS con APIs ya acordadas y consumo estricto de tokens/theme.

## Cuando usarlo

- Despues de Prompt 04 aprobado.
- Inicio de construccion de libreria UI interna.

## Entradas requeridas

- `docs/design-system/03-base-components.md`
- Foundation de tokens/theme operativa.

## Salida esperada (archivos a crear/actualizar)

- Kotlin: `AppButton`, `AppTextField`, `AppCard`, `AppChip`, `AppEmptyState`, `AppLoading`, `AppErrorState`.
- Docs: actualizar `docs/design-system/03-base-components.md`.

## Checklist de calidad

- Sin hardcode visual.
- `Modifier` en API publica.
- Stateless/state hoisting.
- Variantes y estados exigidos.
- KDoc breve y previews principales.

## Riesgos frecuentes

- Reimplementar estilos por componente.
- Mezclar concern de negocio.
- Saltarse estados de loading/disabled/error.

## Plan de ejecucion paso a paso

1. Listar archivos a tocar antes de codificar.
2. Implementar componente por componente segun prioridad.
3. Agregar previews por variantes/estados.
4. Verificar accesibilidad base.
5. Actualizar documento 03 con ejemplos reales.

## Criterio de done

- Componentes compilables, documentados y con previews.
- Guía 03 refleja API final implementada.

## Dependencias con prompts anteriores

- Requiere Prompt 03 y 04.
