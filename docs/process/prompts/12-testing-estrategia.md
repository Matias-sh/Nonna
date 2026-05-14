# Prompt 12 - Testing

## Objetivo del prompt

Definir e implementar estrategia de testing minimo para DS y pantallas principales.

## Cuando usarlo

- Tras consolidar componentes y primeras pantallas bajo MVVM/UDF.

## Entradas requeridas

- Componentes clave a cubrir.
- Pantallas principales seleccionadas.
- Estado actual de dependencias de test.

## Salida esperada (archivos a crear/actualizar)

- `docs/design-system/08-testing.md`
- Propuesta de archivos de test y dependencias necesarias.

## Checklist de calidad

- Estrategia por capas (unit/UI/instrumented).
- Convenciones de `testTag`.
- Casos por componente y por pantalla.
- Definicion de que no se testea aun.

## Riesgos frecuentes

- Tests fragiles centrados en implementacion interna.
- Falta de cobertura de estados (loading/error/success).
- No alinear tests con contratos de API.

## Plan de ejecucion paso a paso

1. Definir cobertura minima viable.
2. Enumerar dependencias y setup.
3. Diseñar casos por componente y pantalla.
4. Priorizar automatizacion incremental.

## Criterio de done

- Documento 08 habilita ejecucion de tests sin ambiguedad.

## Dependencias con prompts anteriores

- Recomendado luego de Prompt 05, 07, 09.
