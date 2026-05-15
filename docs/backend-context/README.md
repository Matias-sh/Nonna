# Backend Context Docs

Esta carpeta centraliza **contexto de backend** para guiar la implementación mobile, sin mezclarlo con los documentos de `docs/design-system` y `docs/features` enfocados en UI/DS.

## Fuente auditada

- Backend local: `C:/Users/Cocido/Downloads/api-nonna-main`
- Módulo principal revisado: `src/schematics/notifications`

## Objetivo

- Documentar contrato real backend (endpoints, payloads, paginación, tipos).
- Dejar auditoría de compatibilidad con Android actual.
- Definir checklist operativo para implementar/corregir notificaciones en mobile.

## Documentos

- `notifications-backend-contract.md`: contrato backend canónico.
- `notifications-mobile-gap-audit.md`: hallazgos de compatibilidad mobile↔backend.
- `notifications-implementation-checklist.md`: plan de implementación y validación.

## Regla de mantenimiento

Cuando backend cambie `DTO`, `query params`, `tipos` o reglas de negocio de notificaciones, actualizar primero esta carpeta y luego ajustar implementación Android.
