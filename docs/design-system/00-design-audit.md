# 00 - Design Audit

## Contexto

Se audito el estado de la UI actual de `Nonna` en la rama `dev_design_system` para establecer una base solida de Design System sin reescribir visualmente la app.

## Estado actual (resumen)

### Fortalezas

- Existe una capa de theme en `ui/theme` con `Theme`, `Color`, `Type`, `Shape`, `Dimens`.
- Ya hay componentes reutilizables (`NonnaButton`, `NonnaTextField`, `NonnaScaffold`, etc.).
- Hay uso de Material 3 como base.
- Hay previews en varios componentes y pantallas.
- Existen ViewModels por feature y estado reactivo con `StateFlow`.

### Gaps detectados

- No existe aun una estructura formal de documentacion en `docs/`.
- Hay mezcla de tokens y valores hardcodeados en pantallas/componentes (`dp`, `Color(...)`, alpha directos).
- APIs de componentes sin contrato unificado (consistencia de parametros y defaults).
- Falta estandar formal de `Route + Screen + UiState + UiEvent` en todas las features.
- Accesibilidad y performance aplicadas de forma parcial, no sistemica.
- Cobertura de testing UI/DS muy limitada.

## Riesgos actuales

- Deriva visual por decisiones locales en pantallas.
- APIs de componentes faciles de usar mal por falta de guias estrictas.
- Mayor costo de mantenimiento cuando crezcan features y equipo.
- Dificultad para medir regresiones de accesibilidad/performance.

## Oportunidad estrategica

Como la app ya esta funcional y visualmente lograda, el foco es:

1. Extraer y consolidar estandares desde lo existente.
2. Formalizar foundation tokens y contratos UI.
3. Migrar incrementalmente por feature, sin cambios big-bang.

## Definicion de exito (fase documental)

- Flujo de 15 etapas documentado y operativo.
- Decisiones tecnicas registradas.
- Checklist de calidad y contribucion disponible.
- Base preparada para implementacion incremental.
