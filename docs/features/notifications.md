# Feature: notifications

## Objetivo

Implementar el flujo completo de notificaciones backend para:

- registrar token de dispositivo,
- listar notificaciones paginadas del usuario autenticado,
- marcar notificación como leída.

## Endpoints integrados

- `POST /notifications/device-token`
- `GET /notifications`
- `PATCH /notifications/{id}/read`

## Arquitectura aplicada

- `NotificationsApi` (capa remote)
- `NotificationsRepository` (mapeo y errores)
- `NotificationsViewModel` (estado UI + paginación + mark-read)
- `NotificationsRoute` (conecta ViewModel + navigation)
- `NotificationsScreen` (renderiza `NotificationsUiState`, emite `NotificationsEvent`)
- `NotificationItemCard` (componente de producto reusable)

## Flujo de token de dispositivo

- `NotificationTokenSyncManager` guarda token pendiente local.
- Si hay sesión activa, intenta sincronizar con backend.
- `MainViewModel` dispara `syncIfLoggedIn()` al validar sesión.
- `MainActivity` solicita token FCM en arranque (vía `PushTokenProvider`) y lo sincroniza.
- `NonnaFirebaseMessagingService` sincroniza automáticamente cuando Firebase rota el token (`onNewToken`).

## Estados de UI cubiertos

- loading inicial
- empty
- success (listado)
- loading more
- error de red/API
- marking as read por item

## Contrato UDF

- `NotificationsUiState`:
  - `items`, `isLoading`, `isLoadingMore`, `onlyUnread`, `errorMessage`, `markingReadIds`
- `NotificationsEvent`:
  - `Back`, `LoadInitial`, `LoadMore`, `ToggleOnlyUnread`, `MarkAsRead`, `OpenSubscriptionCenter`, `ClearError`

## Navegación por tipo

- Si la notificación corresponde a `SUSCRIPCION_RENOVACION_REQUERIDA` (o payload `suscripcion_renovacion_requerida`), la UI muestra CTA para abrir `SubscriptionCenter`.
- Al abrir esa CTA:
  - marca la notificación como leída;
  - navega a `Screen.SubscriptionCenter`;
  - reenvía `paymentId` cuando viene en payload (`pagoId`/`paymentId`).

## Pendientes

- Agregar test instrumentado de `NotificationsScreen` para validar CTA de renovación y navegación esperada.
