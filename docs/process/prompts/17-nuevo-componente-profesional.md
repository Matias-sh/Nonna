# Prompt 17 - Crear componente nuevo profesionalmente

## Objetivo del prompt

Evaluar, diseñar y aprobar API de un componente nuevo antes de implementarlo.

## Cuando usarlo

- Cada vez que se detecta necesidad de nuevo componente reusable.

## Entradas requeridas

- Nombre del componente.
- Casos de uso reales.
- Contexto de reuso esperado.

## Salida esperada (archivos a crear/actualizar)

- Si base: `docs/design-system/03-base-components.md`
- Si producto: `docs/design-system/04-product-components.md`
- Si hay trade-off: `docs/design-system/11-technical-decisions.md`

## Checklist de calidad

- Pertenece al DS o a feature local (decision explicita).
- API clara (obligatorios/opcionales/variantes/estados).
- Accesibilidad y performance consideradas.
- Previews y tests planificados.

## Riesgos frecuentes

- Crear componente gigante.
- Exponer flexibilidad visual innecesaria.
- API inestable que rompa compatibilidad.

## Plan de ejecucion paso a paso

1. Validar pertenencia (DS vs feature local).
2. Definir API y contratos.
3. Documentar limites y do/dont.
4. Aprobar API antes de codificar.

## Criterio de done

- API aprobada, documentada y lista para implementar.

## Dependencias con prompts anteriores

- Requiere 03, 04 y 13.
