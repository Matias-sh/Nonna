# Prompt 03 - Tokens y Theme

## Objetivo del prompt

Construir la capa foundation completa de tokens/theme con Material 3 como base y sin hardcodes visuales.

## Cuando usarlo

- Luego de arquitectura base.
- Antes de implementar o migrar componentes.

## Entradas requeridas

- Auditoria de tokens candidatos.
- Definiciones de marca y tipografia.
- Regla de tokenizacion full foundation.

## Salida esperada (archivos a crear/actualizar)

- Kotlin: Theme, Colors, Typography, Spacing, Shapes, Elevation, ThemePreview.
- Docs: `docs/design-system/02-tokens-theme.md`
- Supuestos: `docs/design-system/11-technical-decisions.md`

## Checklist de calidad

- Tokens primitivos y semanticos.
- Escalas estables (spacing, shape, elevation, type).
- Uso de M3 sin perder identidad visual.
- Ejemplos de consumo y regla de extension de tokens.

## Riesgos frecuentes

- Nombrado visual en lugar de semantico.
- Compatibilidad rota por cambios de tokens sin versionar.
- Dejar valores directos en componentes migrados.

## Plan de ejecucion paso a paso

1. Definir catalogo final de tokens.
2. Alinear nombres semanticos.
3. Implementar archivos foundation.
4. Agregar previews y docs.
5. Registrar supuestos del diseno.

## Criterio de done

- Foundation lista para consumo por componentes base.
- `02-tokens-theme.md` incluye do/dont y guia de extension.

## Dependencias con prompts anteriores

- Requiere Prompt 01 y 02.
