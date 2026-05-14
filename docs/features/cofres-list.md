# Feature: cofres-list

## Objetivo de la pantalla

Permitir al usuario listar, filtrar y abrir cofres, con CTA para crear uno nuevo.

## UiState

- `isLoading: Boolean`
- `cofres: List<CofreUiModel>`
- `errorMessage: String?`

## Eventos

- `SelectTab(tab)`
- `OpenCofre(cofreId)`
- `CreateCofre`

## Flujo de estado

1. `CofresListViewModel` expone `cofres/isLoading/errorMessage`.
2. `CofresListRoute` transforma en `CofresListUiState`.
3. `CofresListScreen` renderiza y emite `CofresListEvent`.
4. `Route` resuelve navegacion.

## Componentes usados

- `AppShell`
- `NonnaTextField`
- `FilterChipsRow`
- `CofreCard`
- `EmptyStateWithButton`

## Estados soportados

- loading
- empty
- success
- filtered empty results

## Decisiones tecnicas

- Se migro de Screen acoplada a ViewModel hacia separacion `Route + Screen`.
- `searchQuery` y `activeFilter` se mantienen locales en Screen por ser estado de presentacion efimero.

## Pendientes

- Unificar `collectAsStateWithLifecycle` en todas las pantallas.
- Agregar tests UI de estados principales.
