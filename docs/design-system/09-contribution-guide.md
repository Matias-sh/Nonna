# 09 - Contribution Guide

## Objetivo

Definir reglas claras para mantener consistencia tecnica y visual en el Design System.

## Flujo de contribucion

1. Revisar documentos de `docs/design-system/`.
2. Proponer cambio con alcance pequeno e incremental.
3. Implementar con tests y previews.
4. Actualizar documentacion relacionada.
5. Registrar impacto en changelog.
6. Solicitar code review senior.

## Checklist de PR

- Sin hardcodes visuales en pantallas.
- API de componentes consistente y stateless.
- `Modifier` en componentes publicos.
- Route/Screen respetado en pantallas con ViewModel.
- Accesibilidad y performance verificadas.
- Tests minimos agregados/actualizados.

## Politica de cambios

- Evitar refactors masivos sin plan documentado.
- Cambios breaking requieren nota explicita en `10-versioning-changelog.md`.
- Decisiones ambiguas se registran en `11-technical-decisions.md`.
