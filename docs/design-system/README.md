# Nonna Design System

Este directorio define la base profesional del Design System de `Nonna` para Jetpack Compose + Material 3.

## Objetivo

Estandarizar y escalar la UI de la app con:

- tokens y theme centralizados (sin hardcode en pantallas),
- componentes reutilizables con APIs claras y estables,
- arquitectura UI moderna con MVVM + UDF,
- accesibilidad, performance, testing y documentacion operativa.

## Flujo oficial de trabajo

1. Prompt maestro del proyecto  
2. Auditoria del producto/diseno  
3. Arquitectura general  
4. Tokens y Theme  
5. APIs de componentes base  
6. Implementacion de componentes base  
7. Componentes especificos del producto  
8. Templates reutilizables  
9. Pantallas con MVVM + UDF  
10. Accesibilidad  
11. Performance / recomposition  
12. Testing  
13. Documentacion  
14. Versionado / changelog  
15. Code review senior

## Documentos

- `00-design-audit.md`: estado actual, fortalezas y gaps.
- `01-ui-architecture.md`: arquitectura UI objetivo y capas.
- `02-tokens-theme.md`: estrategia full foundation de tokens.
- `03-base-components.md`: contrato de componentes base.
- `04-product-components.md`: componentes de negocio/feature.
- `05-templates.md`: templates de pantalla reutilizables.
- `06-accessibility.md`: criterios y checklist de accesibilidad.
- `07-compose-performance.md`: estrategias de performance Compose.
- `08-testing.md`: piramide de tests + cobertura minima por etapa.
- `09-contribution-guide.md`: reglas de contribucion y calidad.
- `10-versioning-changelog.md`: versionado y disciplina de cambios.
- `11-technical-decisions.md`: supuestos, decisiones y trade-offs.

## Principios no negociables

- No hardcodear colores, tamaños, tipografias, paddings, radios ni elevaciones dentro de pantallas.
- Todo valor visual repetible sale de tokens/theme.
- Componentes stateless siempre que sea posible.
- State hoisting obligatorio.
- Todo componente publico recibe `Modifier`.
- Pantallas con ViewModel separan `Route` de `Screen`.
- Pantallas consumen `UiState` y emiten `UiEvent`.
- No logica de negocio ni llamadas a data layer dentro de composables.
- Cada cambio relevante actualiza su documento correspondiente.
