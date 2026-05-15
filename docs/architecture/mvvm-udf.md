# MVVM + UDF en Nonna

## Objetivo

Estandarizar flujo de estado y eventos para todas las pantallas con ViewModel.

## Patrón

- `Route`: conecta navigation + ViewModel.
- `ViewModel`: produce `UiState` y maneja `UiEvent`.
- `Screen`: composable puro, renderiza `UiState` y emite `UiEvent`.

## Flujo de estado y eventos

1. `ViewModel` expone `StateFlow<UiState>`.
2. `Route` colecta estado con `collectAsStateWithLifecycle()`.
3. `Route` pasa `UiState` y `onEvent` a `Screen`.
4. `Screen` renderiza y emite eventos.
5. `Route` traduce eventos en navegacion o acciones de capa superior.

## Reglas

- `UiState` inmutable.
- `UiEvent` sellado por feature.
- `Screen` no ejecuta logica de negocio.
- Side effects controlados en ViewModel o Route.

## Ejemplo aplicado

- Feature cofres:
  - `CofresListUiState`
  - `CofresListEvent`
  - `CofresListRoute`
  - `CofresListScreen`
- Feature profile:
  - `ProfileUiState`
  - `ProfileEvent`
  - `ProfileRoute`
  - `ProfileScreen`
- Feature home:
  - `HomeUiState`
  - `HomeEvent`
  - `HomeRoute`
  - `HomeScreen`
- Feature notifications:
  - `NotificationsUiState`
  - `NotificationsEvent`
  - `NotificationsRoute`
  - `NotificationsScreen`

## Beneficios

- Mejor testabilidad.
- Menos acoplamiento a infraestructura.
- Menor riesgo de recomposition innecesaria por cambios de alcance amplio.
