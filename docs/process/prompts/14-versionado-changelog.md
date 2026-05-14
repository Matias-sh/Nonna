# Prompt 14 - Versionado, changelog y compatibilidad

## Objetivo del prompt

Definir estrategia SemVer y compatibilidad para el DS como libreria interna.

## Cuando usarlo

- Antes de abrir consumo por otros equipos.
- Al introducir cambios de API en componentes/tokens.

## Entradas requeridas

- Estado actual de APIs base/producto.
- Politica de releases del proyecto.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/10-versioning-changelog.md`
- `CHANGELOG.md`

## Checklist de calidad

- Definicion clara de patch/minor/major.
- Breaking changes en Compose explicitados.
- Politica de deprecacion y migraciones.
- Reglas para variantes, renombres y parametros.

## Riesgos frecuentes

- Cambios visuales significativos sin versionado mayor.
- Quitar parametros sin ventana de deprecacion.
- Changelog sin accion requerida para consumidores.

## Plan de ejecucion paso a paso

1. Establecer criterio de severidad de cambios.
2. Definir plantilla de changelog.
3. Documentar deprecaciones y backward compatibility.
4. Incluir ejemplos minimos en docs.

## Criterio de done

- Equipos consumidores pueden migrar sin ambiguedad.

## Dependencias con prompts anteriores

- Requiere 03, 04, 05, 07 y 13.
