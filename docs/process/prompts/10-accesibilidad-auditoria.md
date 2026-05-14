# Prompt 10 - Accesibilidad (auditoria)

## Objetivo del prompt

Auditar accesibilidad de DS y pantallas implementadas, sin modificar codigo en primera pasada.

## Cuando usarlo

- Al cierre de un bloque de implementacion.
- Antes de releases relevantes.

## Entradas requeridas

- Componentes/pantallas actuales.
- Checklist a11y del proyecto.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/06-accessibility.md`

## Checklist de calidad

- `contentDescription` correcto por rol.
- Touch targets minimos.
- Labels y errores claros en inputs.
- Orden de lectura y semantics.
- Casos que dependen solo de color.

## Riesgos frecuentes

- Marcar iconos decorativos como informativos.
- Ignorar estados focused/selected/disabled.
- No priorizar por severidad.

## Plan de ejecucion paso a paso

1. Revisar DS base y pantallas principales.
2. Listar hallazgos con severidad.
3. Proponer correcciones por archivo.
4. Definir reglas para nuevos componentes.

## Criterio de done

- Auditoria accionable con backlog priorizado.

## Dependencias con prompts anteriores

- Recomendado luego de Prompt 05, 07, 09.
