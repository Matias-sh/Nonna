# Prompt 15 - Code review senior

## Objetivo del prompt

Realizar review critica con foco en arquitectura, DS, Compose y riesgos de mantenimiento.

## Cuando usarlo

- Al cierre de milestones.
- Antes de merges grandes o release.

## Entradas requeridas

- Codigo actual del repo.
- Documentacion canonicamente vigente.

## Salida esperada (archivos a crear/actualizar)

- `docs/reviews/code-review-[date].md`
- `docs/design-system/11-technical-decisions.md` (si hay decisiones nuevas)

## Checklist de calidad

- Hallazgos por severidad (critico/alto/medio/bajo).
- Problema concreto + impacto + propuesta.
- Priorizacion y riesgos si no se corrige.

## Riesgos frecuentes

- Review complaciente y no accionable.
- Mezclar opinion sin evidencia.
- No diferenciar deuda tecnica de riesgo funcional.

## Plan de ejecucion paso a paso

1. Revisar arquitectura y separacion de responsabilidades.
2. Revisar APIs publicas y consistencia DS.
3. Revisar accesibilidad, performance, testing.
4. Publicar backlog priorizado con recomendaciones.

## Criterio de done

- Documento de review accionable y defendible en nivel senior/staff.

## Dependencias con prompts anteriores

- Se potencia tras 05, 07, 09, 10, 11, 12, 13, 14.
