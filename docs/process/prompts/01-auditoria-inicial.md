# Prompt 01 - Auditoria inicial del producto/diseno

## Objetivo del prompt

Auditar el producto y la UI actual para identificar patrones, componentes, tokens candidatos y riesgos de escalar sin sistema.

## Cuando usarlo

- Inicio de proyecto.
- Inicio de migracion a Design System.
- Cambios grandes de alcance de producto.

## Entradas requeridas

- Capturas/Figma/wireframes.
- Navegacion/pantallas existentes.
- Baseline de Nonna actual.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/00-design-audit.md`
- Si hay supuestos: `docs/design-system/11-technical-decisions.md`

## Checklist de calidad

- Pantallas detectadas y flujos principales.
- Componentes repetidos y patrones de interaccion.
- Tokens potenciales (color, type, spacing, shape, elevation, icon).
- Estados de componente definidos.
- Riesgos y preguntas abiertas.

## Riesgos frecuentes

- Auditar solo por vistas aisladas.
- No incluir edge cases de estados.
- No diferenciar componente base vs producto.

## Plan de ejecucion paso a paso

1. Levantar inventario de pantallas.
2. Mapear flujos core del usuario.
3. Catalogar componentes repetidos.
4. Extraer tokens candidatos.
5. Separar base components vs product components.
6. Proponer orden de construccion.
7. Registrar supuestos y preguntas.

## Criterio de done

- Documento con 14 puntos del prompt cubiertos y priorizados.
- Riesgos y preguntas accionables para siguiente etapa.

## Dependencias con prompts anteriores

- Usa lineamientos de `docs/design-system/README.md`.
