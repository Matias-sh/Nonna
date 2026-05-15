# Checklist de implementación mobile considerando backend

Checklist operativo para implementación/corrección de notificaciones Android alineada al backend real.

## 1) Contrato de datos

- [ ] `NotificationDto.read` acepta `leido`.
- [ ] `NotificationDto.message` acepta `cuerpo`.
- [ ] Metadata acepta `count` para total.
- [ ] Se validan `tipo` y `payloadJson.type` de backend.

## 2) Registro de token FCM

- [ ] Registrar token solo con sesión activa (JWT válido).
- [ ] Registrar token tras login exitoso.
- [ ] Registrar token al abrir app con sesión persistida.
- [ ] Registrar token en `onNewToken`.
- [ ] Evitar omisión de sync en cambio de cuenta (storage por usuario).

## 3) Centro de notificaciones

- [ ] Consumir `GET /notifications` con paginación.
- [ ] Tab "No leídas" usando `soloNoLeidas=true`.
- [ ] Mostrar `titulo`, `cuerpo`, `createdAt`.
- [ ] Marcar como leída con `PATCH /notifications/{id}/read`.
- [ ] Reflejar estado leído local tras éxito (sin recarga completa obligatoria).

## 4) Navegación por tipo

- [ ] `invitacion_cofre` -> abrir invitaciones/cofre según UX definida.
- [ ] `invitacion_aceptada` -> abrir cofre o lista de cofres.
- [ ] `nueva_version` -> mostrar CTA de actualización.
- [ ] `suscripcion_renovacion_requerida` -> abrir centro de suscripción/pagos.

## 5) Push runtime

- [ ] Foreground: manejo in-app sin perder `data`.
- [ ] Background/cerrada: deep-link o routing al tocar push.
- [ ] Estrategia anti-duplicados (notificación sistema + refresco listado).

## 6) Calidad / observabilidad

- [ ] Unit tests para mapping DTO backend.
- [ ] UI tests `NotificationsScreen` (loading/empty/success/read/error).
- [ ] Logging técnico de fallas de sync token.
- [ ] Métrica básica: ratio de éxito en `POST /notifications/device-token`.

## 7) Seguridad y configuración

- [ ] Verificar `google-services.json` por variante (`debug`, `release`, `qa` si existe).
- [ ] Confirmar `applicationId` coincide con cliente Firebase correspondiente.
- [ ] No versionar secretos (service accounts/credenciales) en repositorios públicos.
