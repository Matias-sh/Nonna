# Prompt 02 - Arquitectura general del proyecto

## Objetivo del prompt

Definir arquitectura Android escalable para Compose + DS + MVVM/UDF sin sobrearquitectura.

## Cuando usarlo

- Despues de auditoria inicial.
- Antes de implementar tokens y componentes.

## Entradas requeridas

- Auditoria (`00-design-audit.md`).
- Estado actual de modulos/packages.
- Reglas de equipo.

## Salida esperada (archivos a crear/actualizar)

- `docs/architecture/app-architecture.md`
- `docs/architecture/feature-template.md`
- `docs/architecture/mvvm-udf.md`
- `docs/design-system/01-ui-architecture.md`

## Checklist de calidad

- Separacion por capas y por feature.
- Ubicacion clara de theme/tokens/components/templates.
- Convenciones de nombres.
- Que entra y que no entra al DS.
- Preparacion para testing y documentacion.

## Riesgos frecuentes

- Mezclar responsabilidades UI y data.
- Definir estructura que no refleja el repo actual.
- Ignorar estrategia incremental.

## Plan de ejecucion paso a paso

1. Definir mapa de capas y ownership.
2. Definir plantilla canonica de feature.
3. Explicar flujo MVVM + UDF y Route/Screen.
4. Documentar reglas de hardcode y testing.
5. Publicar arquitectura objetivo y transicion.

## Criterio de done

- Los 4 documentos quedan consistentes y sin contradicciones.
- Se puede crear una nueva feature siguiendo solo estos docs.

## Dependencias con prompts anteriores

- Requiere salida de Prompt 01.
