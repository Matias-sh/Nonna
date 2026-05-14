# Prompt Crosswalk

Matriz de trazabilidad para evitar contradicciones entre playbook y documentacion canonica.

## Prompt -> fuente canonica principal

1. Auditoria -> `docs/design-system/00-design-audit.md`
2. Arquitectura -> `docs/architecture/app-architecture.md`, `docs/architecture/mvvm-udf.md`, `docs/design-system/01-ui-architecture.md`
3. Tokens/theme -> `docs/design-system/02-tokens-theme.md`
4. APIs base -> `docs/design-system/03-base-components.md`
5. Implementacion base -> `docs/design-system/03-base-components.md`
6. Diseno producto -> `docs/design-system/04-product-components.md`
7. Implementacion producto -> `docs/design-system/04-product-components.md`
8. Templates -> `docs/design-system/05-templates.md`
9. Pantallas MVVM/UDF -> `docs/architecture/mvvm-udf.md`, `docs/features/[feature-name].md`
10. Accesibilidad -> `docs/design-system/06-accessibility.md`
11. Performance -> `docs/design-system/07-compose-performance.md`
12. Testing -> `docs/design-system/08-testing.md`
13. Documentacion general -> `docs/design-system/README.md`, `docs/design-system/09-contribution-guide.md`
14. Versionado/changelog -> `docs/design-system/10-versioning-changelog.md`, `CHANGELOG.md`
15. Code review -> `docs/reviews/code-review-[date].md`, `docs/design-system/11-technical-decisions.md`
16. Nueva pantalla -> `docs/architecture/feature-template.md`, `docs/features/[feature-name].md`
17. Nuevo componente -> `docs/design-system/03-base-components.md` o `docs/design-system/04-product-components.md`
18. Figma/captura -> `docs/features/[feature-name].md` + fuentes DS
19. Entrevista -> `docs/interview/design-system-explanation.md`

## Regla anti-duplicacion

- El contenido operativo vive en `docs/process/prompts/*`.
- El contenido normativo vive en `docs/design-system/*` y `docs/architecture/*`.
- Si hay conflicto, prevalece la fuente canonica.
