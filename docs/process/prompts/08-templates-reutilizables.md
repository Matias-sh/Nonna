# Prompt 08 - Templates reutilizables

## Objetivo del prompt

Definir templates plug and play para acelerar pantallas con consistencia de layout y estados.

## Cuando usarlo

- Luego de tener base/product components en marcha.
- Antes de escalar a multiples pantallas nuevas.

## Entradas requeridas

- Patrones estructurales repetidos en features.
- Catalogo de componentes disponibles.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/05-templates.md`

## Checklist de calidad

- Problema que resuelve cada template.
- API propuesta y slots necesarios.
- Estados soportados (loading/error/empty/success).
- Limites de personalizacion.
- Conexion con pantalla real.

## Riesgos frecuentes

- Templates muy rigidos o demasiado genericos.
- Acoplar templates a ViewModel/repository.
- Permitir customizacion visual que rompe consistencia.

## Plan de ejecucion paso a paso

1. Identificar 3-6 templates de mayor impacto.
2. Definir API y contratos de estado.
3. Documentar componentes reutilizados.
4. Definir guia de adopcion por equipos.

## Criterio de done

- Documento 05 sirve como referencia para crear pantallas nuevas sin reinventar layout.

## Dependencias con prompts anteriores

- Requiere Prompt 05 y 07.
