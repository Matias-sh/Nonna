# Prompt 18 - Pasar pantalla de Figma/captura a Compose sin hacerlo mal

## Objetivo del prompt

Traducir diseno a Compose con enfoque de sistema reusable, no copia aislada.

## Cuando usarlo

- Al implementar pantallas desde Figma o capturas.

## Entradas requeridas

- Fuente visual (Figma/captura).
- Feature y contexto de uso.
- Estado actual de DS.

## Salida esperada (archivos a crear/actualizar)

- Plan de archivos Kotlin a crear/modificar.
- `docs/features/[feature].md`
- Si aplica:
  - `docs/design-system/03-base-components.md`
  - `docs/design-system/04-product-components.md`
  - `docs/design-system/11-technical-decisions.md`

## Checklist de calidad

- Reuso de componentes existentes.
- Identificacion de faltantes (tokens/componentes).
- Definicion de estados y eventos.
- Decision de Route/Screen y necesidad de ViewModel.
- Previews y tests minimos propuestos.

## Riesgos frecuentes

- Hardcode de estilos por urgencia.
- Crear componentes one-off.
- No documentar supuestos del diseno.

## Plan de ejecucion paso a paso

1. Inventariar mapeo Figma -> DS existente.
2. Identificar gaps de tokens/componentes.
3. Definir `UiState`/`Event` y estructura Route/Screen.
4. Proponer archivos y documentacion a actualizar.
5. Implementar solo tras aprobacion.

## Criterio de done

- Plan aprobado con enfoque reusable y escalable.

## Dependencias con prompts anteriores

- Requiere 03, 05, 07, 08, 09.
