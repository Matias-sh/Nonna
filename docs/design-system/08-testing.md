# 08 - Testing

## Objetivo

Asegurar calidad de DS y UI con una estrategia incremental y mantenible.

## Dependencias recomendadas

- `androidx.compose.ui:ui-test-junit4`
- `androidx.compose.ui:ui-test-manifest` (debug)
- `androidx.test.ext:junit`
- `androidx.test.espresso:espresso-core`
- `org.jetbrains.kotlinx:kotlinx-coroutines-test`

## Piramide recomendada

1. Unit tests (ViewModels, reducers, mapeos UI).
2. Compose UI tests (interaccion, estados, accesibilidad basica).
3. Instrumentation selectiva para flujos criticos end-to-end.

## Cobertura minima por etapa

- Foundation tokens: tests de consistencia y disponibilidad.
- Base components: tests de estados (enabled/disabled/error/loading si aplica).
- Feature screens: tests de render por `UiState` y emision de `UiEvent`.

## Casos prioritarios

- Formularios (validacion y errores).
- Listas y filtros.
- Flujos de empty/loading/error/success.
- Navegaciones criticas.

## Convencion de testTag

- Formato: `feature_component_state`.
- Ejemplos:
  - `cofres_search_input`
  - `cofres_fab_create`
  - `button_primary_loading`

## TestTags estrategicos implementados

- `cofres_search_input`
- `cofres_empty_create_button`
- `cofres_fab_create`
- `home_empty_create_button`
- `home_invite_accept_button`
- `home_invite_open_invitations_button`
- `home_invite_close_button`
- `profile_edit_option`
- `profile_subscription_option`
- `profile_invitations_option`
- `profile_logout_action`

## Casos por componente base

- `NonnaButton`: click, disabled, loading label.
- `NonnaTextField`: input change, error text visible, supporting text.
- `EmptyState`: render de titulo/descripcion/accion opcional.
- `NonnaErrorState`: retry visible cuando corresponde.

## Casos por pantalla (primera ola)

- Cofres:
  - loading inicial.
  - empty state con CTA.
  - success con lista filtrable.
  - evento de apertura de cofre.

- Home:
  - empty state y CTA principal.
  - emision de evento `CreateCofre`.

- Profile:
  - render de opciones principales.
  - emision de evento `Logout`.

## Implementado en el repo

- Componentes base (androidTest):
  - `ui/components/NonnaButtonTest.kt`
  - `ui/components/NonnaTextFieldTest.kt`
  - `ui/components/EmptyStateTest.kt`
  - `ui/components/NonnaErrorStateTest.kt`
- Pantallas (androidTest):
  - `ui/screens/cofres/CofresListScreenTest.kt`
  - `ui/screens/home/HomeScreenTest.kt`
  - `ui/screens/profile/ProfileScreenTest.kt`

## Que no se testea todavia

- Snapshot visual exhaustivo por tema/dispositivo.
- Benchmark macro de performance por pantalla.
- E2E completo de todos los flujos de negocio.

## Regla operativa

Ninguna migracion de componente/pantalla se cierra sin test minimo alineado al impacto del cambio.
