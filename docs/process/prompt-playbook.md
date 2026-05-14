# Prompt Playbook

## Objetivo

Estandarizar la ejecucion de prompts 1..19 para evolucionar Nonna con calidad senior en Design System Android Jetpack Compose.

## Reglas globales

- Mantener el diseno actual de Nonna.
- No hardcode visual fuera de tokens/theme.
- Componentes publicos con `Modifier`, API estable y state hoisting.
- Pantallas con ViewModel: `Route` conecta estado/eventos, `Screen` es pura.
- Migracion incremental por feature.
- Toda decision ambigua se registra en `docs/design-system/11-technical-decisions.md`.

## Mapa de dependencias

- 01 alimenta 02 y 03
- 02 define marco de 09, 16, 18
- 03 y 04 habilitan 05 y 07
- 05 y 08 aceleran 09 y 16
- 10, 11 y 12 actuan como calidad transversal
- 13, 14 y 15 consolidan gobernanza
- 19 usa resultados de todos los anteriores

## Bloques de ejecucion

### Bloque A - Fundaciones

- 01 Auditoria
- 02 Arquitectura
- 03 Tokens y Theme
- 04 API de componentes base

### Bloque B - Biblioteca UI

- 05 Implementacion base components
- 06 Diseno product components
- 07 Implementacion product components
- 08 Templates

### Bloque C - Features y calidad

- 09 Pantalla MVVM + UDF
- 10 Accesibilidad
- 11 Performance
- 12 Testing

### Bloque D - Gobernanza

- 13 Documentacion general
- 14 Versionado y changelog
- 15 Code review senior

### Bloque E - Operacion continua

- 16 Nueva pantalla
- 17 Nuevo componente
- 18 Figma/captura a Compose
- 19 Preparacion entrevista

## Anti-patrones a evitar

- Pantallas sueltas sin sistema reusable.
- Componentes con logica de negocio.
- APIs de componentes con demasiados booleans ambiguos.
- Documentacion desactualizada respecto al codigo.
- Cambios visuales sin versionado/changelog.

## Definicion de done por prompt

1. Entregable documental actualizado.
2. Checklist de calidad completo.
3. Riesgos y pendientes explicitados.
4. Crosslink a docs canonicas existentes.
5. Si aplica implementacion: previews + tests minimos.

## Fuentes canonicas

- `docs/design-system/README.md`
- `docs/design-system/00-design-audit.md`
- `docs/design-system/01-ui-architecture.md`
- `docs/design-system/02-tokens-theme.md`
- `docs/design-system/03-base-components.md`
- `docs/design-system/04-product-components.md`
- `docs/design-system/05-templates.md`
- `docs/design-system/06-accessibility.md`
- `docs/design-system/07-compose-performance.md`
- `docs/design-system/08-testing.md`
- `docs/design-system/10-versioning-changelog.md`
- `docs/design-system/11-technical-decisions.md`
- `docs/architecture/app-architecture.md`
- `docs/architecture/feature-template.md`
- `docs/architecture/mvvm-udf.md`
