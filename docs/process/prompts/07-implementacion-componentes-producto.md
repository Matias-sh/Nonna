# Prompt 07 - Implementar componentes especificos del producto

## Objetivo del prompt

Implementar componentes del dominio priorizando reutilizacion y consistencia visual.

## Cuando usarlo

- Con `04-product-components.md` aprobado.
- Antes de refactor fuerte de pantallas.

## Entradas requeridas

- Priorizacion de componentes por frecuencia de uso.
- Base components ya implementados.

## Salida esperada (archivos a crear/actualizar)

- Kotlin de componentes producto priorizados.
- Update en `docs/design-system/04-product-components.md`.

## Checklist de calidad

- Reuso de base components.
- Estados realistas y edge cases.
- Previews con datos fake coherentes.
- Sin logica de negocio ni data calls.

## Riesgos frecuentes

- Implementar componentes unicos sin valor reusable.
- Exponer APIs inestables.
- No cubrir variantes usadas por pantallas reales.

## Plan de ejecucion paso a paso

1. Priorizar top 3-5 componentes mas repetidos.
2. Implementar con API definida en docs.
3. Agregar previews y KDoc si API publica.
4. Validar accesibilidad minima.
5. Actualizar 04 con estado de implementacion y pendientes.

## Criterio de done

- Componentes listos para ser consumidos por mas de una pantalla.

## Dependencias con prompts anteriores

- Requiere Prompt 06.
