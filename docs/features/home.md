# Feature: home

## Objetivo de la pantalla

Dar contexto inicial al usuario, acceso rapido a acciones clave y continuidad sobre su ultimo cofre.

## UiState

- `cofres: List<CofreUiModel>`
- `user: UserDto?`
- `isLoading: Boolean`
- `errorMessage: String?`
- `featuredInvitation: CofreInvitationUiModel?`
- `invitationAcceptLoading: Boolean`
- `invitationAcceptError: String?`

## Eventos

- `SelectTab(tab)`
- `CreateCofre`
- `AddMemory`
- `ContinueCofre(cofreId)`
- `OpenInvitations`
- `AcceptInvitation(invitationId)`
- `ClearInvitationAcceptError`
- `DismissFeaturedInvitation`

## Flujo de estado

1. `HomeViewModel` expone `StateFlow` con estado de home e invitaciones.
2. `HomeRoute` colecta estado con `collectAsStateWithLifecycle`.
3. `HomeScreen` renderiza `HomeUiState` y emite `HomeEvent`.
4. `Route` conecta eventos con navegacion o acciones del ViewModel.

## Componentes usados

- `AppShell`
- `NonnaButton`
- `EmptyStateWithButton`
- `CofreCard` (bloques de continuidad)
- Dialog de invitaciones (local de feature)

## Estados soportados

- loading inicial
- empty sin cofres
- success con secciones de contenido
- invitacion destacada con aceptacion/error

## Decisiones tecnicas

- Se mantuvo la logica de preferencias locales en Screen (estado UI efimero y persistencia local de hints).
- Se separo la conexion con ViewModel y navegacion en `HomeRoute`.

## Pendientes

- Evaluar extraccion del dialog de invitacion a componente de producto reusable.
- Incorporar tests UI para eventos de invitaciones y estados de home.
