# Prompt 11 - Performance y recomposition (auditoria)

## Objetivo del prompt

Detectar problemas de recomposition y performance en Compose antes de optimizar codigo.

## Cuando usarlo

- Tras implementar componentes o pantallas nuevas.
- En regresiones de fluidez o tiempos de render.

## Entradas requeridas

- Componentes/pantallas foco.
- Checklist de performance Compose.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/07-compose-performance.md`

## Checklist de calidad

- `remember` y `derivedStateOf` bien aplicados.
- keys en listas perezosas.
- parametros estables en componentes reutilizados.
- side effects controlados.
- separacion de responsabilidades en composables grandes.

## Riesgos frecuentes

- Micro-optimizaciones sin impacto.
- No distinguir problema estructural vs local.
- Ignorar lambdas/objetos inestables.

## Plan de ejecucion paso a paso

1. Auditar componentes de mayor uso.
2. Auditar pantallas de mayor complejidad.
3. Priorizar hallazgos por impacto.
4. Definir checklist para code review.

## Criterio de done

- Documento 07 con problemas, prioridad y plan de correccion.

## Dependencias con prompts anteriores

- Recomendado luego de Prompt 05, 07, 09.
