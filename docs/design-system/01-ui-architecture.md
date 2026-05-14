# 01 - UI Architecture

## Objetivo

Definir una arquitectura UI escalable para Compose que garantice separacion de responsabilidades, reusabilidad y evolucion segura.

## Capas objetivo

### 1) Foundation (`ui/designsystem/foundation`)

- Tokens de color, spacing, typography, shape, elevation, motion, icon-size.
- Roles semanticos de color y estilo.
- Acceso central via `NonnaTheme` y extensiones.

### 2) Base Components (`ui/designsystem/components/base`)

- Componentes atomicos y moleculares reutilizables.
- APIs estables, stateless y orientadas a state hoisting.
- Sin dependencias de negocio.

### 3) Product Components (`ui/designsystem/components/product`)

- Componentes compuestos especificos de Nonna (ej: cards de cofre, bloques de memoria).
- Consumen foundation/base, no redefinen tokens locales.

### 4) Templates (`ui/designsystem/templates`)

- Estructuras de layout reutilizables para pantallas completas.
- Reducen duplicacion de scaffolds y patrones repetidos.

### 5) Features (`ui/features/<feature>/presentation`)

- `FeatureRoute`: integra ViewModel, colecta estado, dispara eventos de navegacion.
- `FeatureScreen`: composable puro que recibe `UiState` y `onEvent`.
- `FeatureUiState`: estado inmutable serializable para preview/testing.
- `FeatureUiEvent`: intenciones de usuario.

## Regla de oro de arquitectura

- Data/business logic vive fuera de composables.
- Composable de pantalla no llama APIs ni repositories.
- Toda interaccion de usuario se expresa como `UiEvent`.

## Estrategia de migracion

- Incremental por feature (no big-bang).
- Primero foundation + componentes base.
- Luego componentes de producto.
- Finalmente, alineacion de pantallas al patron Route/Screen con UDF.
