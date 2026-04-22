# API audit: verificacion e invitaciones

Fecha: 2026-04-22
Ambiente: `https://apinonna.pushsoftware.com.ar`

## Fuente de contrato

- OpenAPI: `GET /api-json`
- Endpoints relevantes detectados:
  - `POST /auth/send-verification-email`
  - `POST /auth/verify-email`
  - `GET /cofre-recuerdos/mis-invitaciones-pendientes`
  - `GET /cofre-recuerdos/mis-invitaciones-enviadas`
  - `POST /cofre-recuerdos/invitaciones/{id}/aceptar`
  - `POST /cofre-recuerdos/invitaciones/{id}/rechazar`
  - `DELETE /cofre-recuerdos/invitaciones/{id}`
  - `POST /cofre-recuerdos/invitar`

## Validacion runtime (sin token)

Se ejecutaron requests directas sin `Authorization` para comprobar disponibilidad y seguridad:

- `POST /auth/send-verification-email` -> `401`
- `POST /auth/verify-email` -> `401`
- `GET /cofre-recuerdos/mis-invitaciones-pendientes` -> `401`
- `GET /cofre-recuerdos/mis-invitaciones-enviadas` -> `401`
- `POST /cofre-recuerdos/invitaciones/1/aceptar` -> `401`
- `POST /cofre-recuerdos/invitaciones/1/rechazar` -> `401`
- `DELETE /cofre-recuerdos/invitaciones/1` -> `401`
- `POST /cofre-recuerdos/invitar` -> `401`

Conclusiones:

1. Los endpoints existen y estan protegidos por JWT, consistente con OpenAPI.
2. La validacion de casos autenticados (incluyendo invitar no registrados) requiere token de usuario real.
3. En OpenAPI hay señal mixta sobre no registrados:
   - `InvitadosCofreFullDTO` permite `persona = null` (sugiere soporte).
   - `POST /cofre-recuerdos/invitar` documenta `404` si no existe algun usuario.
4. Implementacion Android debe contemplar ambos comportamientos con mensajes claros de fallback.
