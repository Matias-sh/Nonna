# 10 - Versioning & Changelog

## Objetivo

Versionar la evolucion del Design System y dejar trazabilidad de cambios funcionales/visuales.

## Esquema recomendado

- `major`: cambios breaking de API o comportamiento base.
- `minor`: nuevos componentes/variantes sin romper compatibilidad.
- `patch`: fixes o mejoras internas sin impacto de contrato.

## Formato de changelog

Cada entrada debe incluir:

- fecha,
- version,
- alcance,
- impacto,
- accion requerida (si aplica).

## Politica

- Todo cambio en componentes base o tokens debe registrarse.
- Si afecta features existentes, incluir plan de migracion corto.
- Cambios sin nota de changelog no se consideran cerrados.
