# Prompt 06 - Diseno de componentes especificos del producto

## Objetivo del prompt

Diseñar componentes del dominio Nonna reutilizando base components, sin logica de negocio.

## Cuando usarlo

- Tras consolidar componentes base.
- Antes de migrar pantallas masivas.

## Entradas requeridas

- Inventario de componentes repetidos del producto.
- `docs/design-system/03-base-components.md`
- `docs/design-system/00-design-audit.md`

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/04-product-components.md`
- Si aplica: `docs/design-system/11-technical-decisions.md`

## Checklist de calidad

- Responsabilidad y API por componente.
- Variantes, estados y edge cases.
- Base component reutilizado explicitado.
- Datos requeridos y callbacks claros.

## Riesgos frecuentes

- Componentes demasiado acoplados a una pantalla.
- Incluir llamadas a repository/ViewModel.
- Duplicar estilos que ya resuelve el DS base.

## Plan de ejecucion paso a paso

1. Seleccionar componentes por reutilizacion real.
2. Definir API publica y limites.
3. Documentar estados y accesibilidad.
4. Declarar que NO debe hacer cada componente.
5. Registrar supuestos pendientes.

## Criterio de done

- Documento 04 suficiente para implementar sin rediscutir contratos.

## Dependencias con prompts anteriores

- Requiere Prompt 05 (o al menos 04 aprobado).
