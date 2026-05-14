# Prompt 13 - Documentacion general

## Objetivo del prompt

Consolidar la documentacion del DS como libreria interna consumible por multiples squads.

## Cuando usarlo

- Al cerrar una fase relevante de implementacion.
- Antes de escalar equipo/colaboradores.

## Entradas requeridas

- Estado real de tokens, componentes, templates y arquitectura.
- Cambios recientes validados.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/README.md`
- `docs/design-system/02-tokens-theme.md`
- `docs/design-system/03-base-components.md`
- `docs/design-system/04-product-components.md`
- `docs/design-system/05-templates.md`
- `docs/design-system/06-accessibility.md`
- `docs/design-system/07-compose-performance.md`
- `docs/design-system/08-testing.md`
- `docs/design-system/09-contribution-guide.md`
- `docs/architecture/feature-template.md`
- `docs/architecture/mvvm-udf.md`

## Checklist de calidad

- Claridad practica y orientada a dev Android.
- Ejemplos solo donde agregan valor.
- Convenciones y do/dont coherentes.
- Enlaces internos completos.

## Riesgos frecuentes

- Documentacion extensa pero poco operativa.
- Drift entre docs y codigo.
- Falta de guia de adopcion para nuevas features.

## Plan de ejecucion paso a paso

1. Auditar docs existentes.
2. Actualizar secciones desalineadas.
3. Estandarizar tono/estructura.
4. Validar enlaces internos.

## Criterio de done

- Documentacion autoexplicativa para onboard de dev nuevo.

## Dependencias con prompts anteriores

- Requiere progreso real de 03, 05, 07, 08, 09, 10, 11, 12.
