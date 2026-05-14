# Prompt 04 - Diseno de APIs de componentes base

## Objetivo del prompt

Diseñar APIs publicas estables para componentes base del DS antes de implementar.

## Cuando usarlo

- Con tokens/theme ya definidos.
- Antes de escribir implementacion de componentes base.

## Entradas requeridas

- `docs/design-system/02-tokens-theme.md`
- Catalogo de componentes iniciales.
- Restricciones de accesibilidad/performance.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/03-base-components.md`
- Si aplica: `docs/design-system/11-technical-decisions.md`

## Checklist de calidad

- Responsabilidad clara por componente.
- API con parametros obligatorios/opcionales y variantes.
- Estados soportados.
- Reglas de accesibilidad/performance.
- Ejemplos correctos/incorrectos.

## Riesgos frecuentes

- APIs gigantes o ambiguas.
- Permitir libertad visual no controlada.
- No definir limites (what not to allow).

## Plan de ejecucion paso a paso

1. Definir contrato base por componente.
2. Evaluar slots vs parametros explicitos.
3. Documentar estados y variantes.
4. Definir previews y pruebas minimas.
5. Cerrar decisiones en `11-technical-decisions.md` si hay trade-offs.

## Criterio de done

- Documento apto para implementar sin redefinir API durante codigo.

## Dependencias con prompts anteriores

- Requiere Prompt 03.
