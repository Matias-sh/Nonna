# 03 - Base Components API

## Objetivo

Definir contrato estable para componentes base del Design System.

## Reglas API obligatorias

- Cada componente publico recibe `modifier: Modifier = Modifier`.
- Stateless por defecto.
- State hoisting: valor + callback (`value`, `onValueChange`, etc.).
- Nombres de parametros claros y orientados a intencion.
- Defaults seguros para evitar uso incorrecto.
- Evitar booleans ambiguos cuando un enum mejora legibilidad.

## Contrato de calidad

- Sin hardcoded visual values.
- Sin dependencia a capa data/domain.
- Comportamiento accesible por defecto (semantics, touch target, labels).
- Estabilidad de parametros para minimizar recomposition.

## Componentes base prioritarios

1. `NonnaButton` (variants, sizes, states, icon support).
2. `NonnaTextField` / `NonnaTextArea`.
3. `NonnaCard` base wrappers.
4. `NonnaChip`.
5. `NonnaLoading` y `NonnaErrorState`.
6. `NonnaScaffold` y shells de layout.

## Implementados en primera ola

- `NonnaButton`: variantes `Primary`, `Secondary`, `Outline`, `Ghost`, `Destructive`; tamaños `Small`, `Medium`, `Large`; estados `enabled/disabled`.
- `NonnaTextField` y `NonnaTextArea`: state hoisting, label, placeholder, helper, error, iconos, single/multi-line.
- `NonnaCard`: variantes `Elevated`, `Outlined`, `Filled`.
- `NonnaChip`: chip seleccionable reusable para filtros/tags.
- `NonnaLoading`: estado loading base.
- `NonnaErrorState`: estado de error base con retry opcional.

## Ejemplos reales de uso

- `NonnaButton(text = "Guardar", onClick = ...)`
- `NonnaTextField(value = state.query, onValueChange = ...)`
- `NonnaCard(variant = NonnaCardVariant.Outlined) { ... }`
- `NonnaChip(text = "Mios", selected = true, onClick = ...)`
- `NonnaLoading(message = "Cargando cofres...")`
- `NonnaErrorState(title = "...", description = "...", retryLabel = "...", onRetry = ...)`

## Estados soportados

- `default`
- `disabled` (button/chip/input)
- `error` (input + error state component)
- `loading` (loading state component)
- `selected` (chip)
- `focused` (input)

## Reglas de accesibilidad

- Iconos decorativos con `contentDescription = null`.
- Iconos/interacciones informativas con descripcion explicita.
- Touch target minimo de 48dp para elementos interactivos.
- Inputs con label y mensajes de error claros.

## Do / Don't

- Do: usar enums para variantes/tamaños.
- Do: exponer `Modifier` y callbacks claros.
- Don't: permitir colores arbitrarios por parametro publico.
- Don't: incluir llamadas a ViewModel/repository dentro de componentes.

## Criterio de salida por componente

- API documentada.
- Previews de estado normal, error, disabled, loading (si aplica).
- Test minimo de contrato visual/comportamental.
- Registro en changelog.
