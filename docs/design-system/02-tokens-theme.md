# 02 - Tokens & Theme

## Decision principal y alcance implementado

Se adopta estrategia **full foundation desde el inicio**.

Implementado en la base UI:

- `ui/theme/Spacing.kt`
- `ui/theme/Elevation.kt`
- `ui/theme/Color.kt`
- `ui/theme/Theme.kt`
- `ui/theme/Type.kt`
- `ui/theme/Shape.kt`
- `ui/theme/NonnaThemePreview.kt`

## Capas de tokens

### Primitive tokens

- Color primitivos (brand, neutral, feedback).
- Spacing scale (4dp grid) via `NonnaSpacing`:
  - `none`, `xs`, `sm`, `md`, `lg`, `xl`, `xxl`
- Typography scale (familia, peso, size, line-height).
- Shape/radius.
- Elevation via `NonnaElevation`:
  - `none`, `sm`, `md`, `lg`, `xl`
- Motion (durations, easing, spring specs).
- Icon sizes.

### Semantic tokens

- Surface roles (background, card, input, elevated).
- Content roles (primary text, secondary text, inverse).
- Action roles (primary, secondary, destructive).
- Feedback roles (success, warning, error, info) + aliases en `NonnaColors`.

## Ejemplos de uso

- Spacing: `Modifier.padding(NonnaSpacing.lg)`
- Elevation: `CardDefaults.cardElevation(defaultElevation = NonnaElevation.lg)`
- Text role: `NonnaColors.textSecondary`

## Reglas de uso

- Pantallas no usan valores visuales directos.
- Componentes base solo usan tokens/theme.
- Si un valor visual se repite, se promueve a token.
- No agregar nuevos tokens sin documentar su razon en `11-technical-decisions.md`.

## Do / Don't

- Do: usar `NonnaSpacing` y `NonnaElevation` dentro de DS.
- Do: usar aliases semanticos para texto/feedback cuando aplique.
- Don't: agregar `dp` o `Color(...)` nuevos en pantallas.
- Don't: crear tokens ad-hoc por feature sin pasar por la capa foundation.

## Scope inicial de normalizacion

1. Consolidar naming de tokens actuales en una convension unica.
2. Eliminar hardcodes de `dp`/`Color(...)` fuera de foundation (iterativo por feature).
3. Definir API de acceso consistente a tokens semanticos.
4. Mantener compatibilidad con componentes ya existentes durante migracion.

## Modo oscuro

- Estado actual: no prioritario.
- Decision: preparar arquitectura para dark mode ahora, implementacion funcional en fase futura.
