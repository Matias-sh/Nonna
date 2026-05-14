# Feature: profile

## Objetivo de la pantalla

Mostrar datos del usuario, estado de suscripcion y accesos a acciones de cuenta.

## UiState

- `user: UserDto?`
- `suscripcion: SuscripcionActualDto?`
- `isLoading: Boolean`
- `errorMessage: String?`

## Eventos

- `SelectTab(tab)`
- `EditProfile`
- `OpenSubscriptionCenter`
- `OpenInvitations`
- `Logout`

## Flujo de estado

1. `ProfileViewModel` expone `StateFlow`.
2. `ProfileRoute` colecta con `collectAsStateWithLifecycle`.
3. `ProfileScreen` renderiza `ProfileUiState`.
4. Eventos se resuelven en `Route`.

## Componentes usados

- `AppShell`
- `NonnaBottomFeedbackBanner`
- `OptionCard` (local de feature)
- `SubscriptionSummaryCard` (local de feature)

## Estados soportados

- loading parcial de perfil
- success con datos de usuario
- error de carga con feedback banner

## Decisiones tecnicas

- Se separo `Route` y `Screen` sin alterar visual.
- El estado de feedback (visible/mensaje/tipo) queda local en `Screen` por ser efimero de UI.

## Pendientes

- Evaluar mover `OptionCard` a componentes de producto si se reutiliza en mas features.
- Agregar tests de UI para eventos principales del perfil.
