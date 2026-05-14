# 07 - Compose Performance

## Objetivo

Prevenir recomposiciones innecesarias y mantener UI fluida en features actuales y futuras.

## Problemas detectados

### Prioridad alta

- Pantallas complejas con mezcla Route+Screen previa (ej: listado de cofres) generaban mayor acoplamiento de estado y menor testabilidad.
  - Accion tomada: separacion inicial a `CofresListRoute` + `CofresListScreen` + `UiState` + `Event`.

### Prioridad media

- Hardcodes de spacing/elevation en componentes base incrementaban drift visual y retrabajo.
  - Accion tomada: introduccion de `NonnaSpacing` y `NonnaElevation` como foundation.

### Prioridad media

- Uso mixto de `collectAsState` en pantallas con lifecycle, cuando corresponde `collectAsStateWithLifecycle`.
  - Accion tomada: migracion inicial en flujo de cofres.

## Reglas principales

- Preferir parametros estables e inmutables.
- Usar `remember` para objetos costosos o dependencias estables.
- Usar `derivedStateOf` para derivaciones de estado frecuentes.
- Definir `key` en listas (`LazyColumn`, `LazyVerticalGrid`).
- Evitar pasar lambdas recreadas sin necesidad.
- Evitar crear estados locales no necesarios en componentes profundos.

## Guardrails

- No mover logica de negocio a composables para "optimizar rapido".
- Revisar `collectAsState` vs `collectAsStateWithLifecycle` segun contexto.
- Mantener separacion Route/Screen para limitar alcance de recomposition.

## Checklist para code review

- ¿La pantalla separa Route y Screen?
- ¿El estado de pantalla se modela en `UiState`?
- ¿Hay listas con `key` estable?
- ¿Se evita crear objetos/lambdas no estables en cada recomposicion?
- ¿Se usa `remember`/`derivedStateOf` cuando corresponde?

## Medicion recomendada

- Inspeccion de recomposition en tooling de Compose.
- Baselines comparables antes/despues de migraciones grandes.
