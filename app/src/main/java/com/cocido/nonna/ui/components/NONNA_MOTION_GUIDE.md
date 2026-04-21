# NONNA Motion Guide

Este documento define el lenguaje de animación cálido y consistente de NONNA.

## Tokens base

- Duraciones: `150ms` (rápida), `250ms` (media), `400ms` (transiciones de pantalla)
- Stagger por item: `60ms`
- Springs:
  - `buttonSpring`: interacción de botones y press states
  - `bounceSpring`: rebote suave (FAB, icono tab activo)
  - `navSpring`: desplazamientos de navegación y sidebar

Todos los tokens viven en `NonnaMotion.kt`.

## Patrones de uso

- `nonnaInteractiveScale(...)`
  - Press feedback estándar: `1f -> 0.98f`
- `nonnaInteractiveElevation(...)`
  - Cards: `2dp -> 8dp` al interactuar
- `NonnaStaggerItem(...)`
  - Entrada de listas/grids con fade + slide up

## Reglas rápidas

- Mantener la animación sutil y legible (sin exagerar amplitud)
- Priorizar springs para microinteracciones
- Usar `400ms` solo en cambios de pantalla
- Usar stagger en bloques con repetición visual (cards, nodos, acciones)
