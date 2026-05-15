# Auditoría de compatibilidad: Mobile vs Backend (notificaciones)

Estado auditado sobre:
- Backend: `api-nonna-main`
- App Android: `Nonna`

## Estado de implementación (2026-05-15)

- Resuelto: mapeo `cuerpo` en mensaje.
- Resuelto: mapeo `leido` en flag de lectura.
- Resuelto: mapeo `metadata.count` hacia total de elementos.
- Resuelto: sync de token FCM con scope por usuario/sesión para evitar omisiones en cambio de cuenta.
- Resuelto: UX/navegación para `suscripcion_renovacion_requerida` con CTA desde notificación hacia centro de suscripción (incluye `paymentId` cuando viene en payload).

## Hallazgos críticos/altos (estado histórico)

## Alto - Campo de leído no mapea correctamente (RESUELTO)

- Backend entrega `leido`.
- Android DTO actual espera `leida` (y alternates `read/isRead`), no incluye `leido`.
- Impacto: notificaciones pueden verse siempre como no leídas en UI aunque backend ya las marque.

Acción:
- Agregar `leido` en alternates de `NotificationDto.read`.

## Alto - Campo de cuerpo no mapea correctamente (RESUELTO)

- Backend entrega `cuerpo`.
- Android DTO actual espera `mensaje` (y alternates `message/body/descripcion`), no incluye `cuerpo`.
- Impacto: mensaje puede quedar vacío en tarjetas.

Acción:
- Agregar `cuerpo` en alternates de `NotificationDto.message`.

## Hallazgos medios (estado histórico)

## Medio - Metadato total no toma `count` (RESUELTO)

- Backend `metadata` usa `count` como total de elementos.
- Android `PageMetadataDto` usa `totalItems`.
- Impacto: el total puede degradar a fallback local y afectar decisiones de paginación/UX.

Acción:
- Aceptar `count` como alternate de `totalItems` o modelar `count` explícitamente.

## Medio - Tipo backend nuevo aún no contemplado en UX (RESUELTO)

- Backend ya envía `SUSCRIPCION_RENOVACION_REQUERIDA` / `suscripcion_renovacion_requerida`.
- UI actual no define navegación/cta explícita para ese caso.

Acción:
- Definir comportamiento de producto para notificaciones de renovación (abrir centro de suscripción/pagos).

## Medio - Sin test específico del contrato de notificaciones

- No hay pruebas android instrumentadas focalizadas en `NotificationsScreen` ni en mapping DTO backend real.

Acción:
- Agregar tests de parser y tests UI de estado leído/no leído + cuerpo.

## Hallazgos bajos

## Bajo - `sortBy` se envía pero backend ordena por `id DESC`

- Es funcional, pero induce falsa expectativa de sort dinámico.

Acción:
- Documentar en cliente que el orden efectivo hoy es fijo por backend.

## Bajo - Riesgo de sync por token global entre usuarios (RESUELTO)

- En mobile, `last_synced_fcm_token` está guardado globalmente y no por usuario.
- Si un mismo dispositivo cambia de cuenta, puede omitir re-registro para el nuevo usuario.

Acción:
- Persistir `last_synced_fcm_token` por `userId` o por clave compuesta `userId+token`.

## Resultado general

- Integración actual: **funcional y alineada** al contrato backend auditado.
- Nivel de alineación con contrato backend real: **alto**, con pendiente principal en test UI instrumentado específico de la pantalla de notificaciones.
