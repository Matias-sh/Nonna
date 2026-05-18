# Auditoría de Errores y Flujos (Producción)

Fecha: 2026-05-18

## Objetivo

Unificar el manejo de errores para que la app muestre mensajes accionables y consistentes, con foco en límites de plan y validaciones de backend.

## Hallazgos críticos

1. El backend responde `400` con `message` genérico (`Datos de entrada no válidos.`) y un `details` accionable.
2. La app estaba priorizando `message` y ocultaba el `details` (causa del mensaje confuso al crear el tercer cofre).
3. Había repositorios que mostraban JSON crudo o mensajes técnicos en vez de texto normalizado.
4. En la tarjeta de suscripción, los límites podían mostrarse por debajo del uso real si llegaban datos inconsistentes.

## Cambios aplicados

- `NetworkErrorParser` ahora prioriza `details/detail` (incluyendo `errorDetails`) antes de `message`.
- Se agregó normalización para casos genéricos de validación cuando no llega detalle útil.
- `CreateCofreViewModel` valida límite de cofres antes de enviar (usa `GET /suscripciones/me`).
- `AddMemoryViewModel` valida límite de recuerdos antes de enviar (usa `GET /suscripciones/me`).
- Repositorios con errores crudos migrados a parser central:
  - `CofreRepository`
  - `RecuerdosRepository`
  - `ArbolFamiliarRepository`
  - `EmocionesRepository`
- `ProfileScreen` ahora resuelve límites visibles con criterio defensivo:
  - usa el máximo entre `suscripcion.limites`, `plan` y `uso` para evitar subreportar.

## Criterio de consistencia de plan

Fuente de verdad de UI:

1. `GET /suscripciones/me` (`plan`, `limites`, `uso`).
2. Si hay discrepancia temporal entre campos, se evita mostrar límites menores al uso actual.

Esto elimina casos donde se mostraba “1 cofre” cuando el usuario ya tiene 2 creados.

## Casos cubiertos de límite

- Límite de cofres propios.
- Límite de recuerdos.
- Mensajes de backend tipo:
  - `Alcanzaste el límite de N cofre(s) para tu plan X.`
  - `Alcanzaste el límite de N recuerdo(s) para tu plan X.`

## Riesgo residual

- Si backend no envía `details` en algunos endpoints antiguos, la app mostrará fallback normalizado.
- Si hay divergencia real entre reglas de negocio y datos de `suscripciones/me`, debe corregirse en backend para consistencia total.

## Validación ejecutada

- `:app:testDebugUnitTest --tests "com.cocido.nonna.data.repository.NetworkErrorParserTest"`
- `:app:compileDebugKotlin`

