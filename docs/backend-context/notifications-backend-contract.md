# Notificaciones: contrato backend (canónico)

Documento basado en el backend auditado en `api-nonna-main`.

## Endpoints

## `POST /notifications/device-token`

- Auth: `Bearer JWT` requerido.
- Body:
  - `fcmToken` (string, requerido)
  - `platform` (`android | ios | web`, opcional, default backend `android`)
  - `deviceId` (string, opcional)
  - `deviceName` (string, opcional)
- Respuesta:
  - `200`: `{ "message": "Token registrado correctamente" }`

Regla backend:
- Idempotencia por `(usuarioId + fcmToken)`.
- Si ya existe, actualiza `platform/deviceId/deviceName`.

## `GET /notifications`

- Auth: `Bearer JWT` requerido.
- Query:
  - `pageNumber` (opcional, default `1`)
  - `pageSize` (opcional, default backend `9999`, max `9999`)
  - `q` (opcional)
  - `sortBy` (opcional, actualmente no impacta orden real)
  - `soloNoLeidas` (opcional, boolean)
- Orden real de datos: `id DESC` (más nueva primero).

Respuesta:

```json
{
  "data": [
    {
      "id": 1,
      "tipo": "INVITACION_COFRE",
      "titulo": "Invitación a un cofre",
      "cuerpo": "Silvana te invitó al cofre \"Abuela\".",
      "payloadJson": {
        "type": "invitacion_cofre",
        "cofreId": "123"
      },
      "leido": false,
      "createdAt": "2025-02-10T15:30:00.000Z"
    }
  ],
  "metadata": {
    "count": 1,
    "pageSize": 20,
    "pageNumber": 1,
    "totalPages": 1
  }
}
```

## `PATCH /notifications/{id}/read`

- Auth: `Bearer JWT` requerido.
- Regla: solo si la notificación pertenece al usuario autenticado.
- Respuesta:
  - `200`: `{ "message": "Notificación marcada como leída" }`
- Idempotente si ya estaba leída.

## Tipos de notificación persistidos (`tipo`)

- `INVITACION_COFRE`
- `INVITACION_ACEPTADA`
- `NUEVA_VERSION`
- `SUSCRIPCION_RENOVACION_REQUERIDA`

## `payloadJson.type` observado

- `invitacion_cofre` (`cofreId`)
- `invitacion_aceptada` (`cofreId`)
- `nueva_version` (`version` opcional)
- `suscripcion_renovacion_requerida` (`pagoId`)

## Eventos backend que disparan push + persistencia

- Invitar a cofre (`cofre-recuerdos.service`) -> `notifyInvitacionCofre`.
- Aceptar invitación (`cofre-recuerdos.service`) -> `notifyInvitacionAceptada`.
- Renovación requerida (`pagos-suscripcion.service`) -> `notifyRenovacionSuscripcionRequerida`.
- Nueva versión: disponible como método `notifyNuevaVersion`.

## Notas operativas

- Backend soporta múltiples tokens por usuario (varios dispositivos).
- No hay endpoint explícito de desregistro de token en logout.
- En errores FCM por token inválido, backend loguea warning; no elimina token automáticamente.
