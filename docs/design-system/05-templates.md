# 05 - Templates Reutilizables

## Objetivo

Reducir duplicacion de estructura de pantallas con templates Compose reutilizables.

## Templates objetivo

- `ListTemplate`: header + filtros + contenido paginado/lista + empty/error/loading.
- `DetailTemplate`: top app bar + hero/media + bloques de contenido + acciones.
- `FormTemplate`: secciones, validacion, CTA sticky, estados submitting/error.
- `DashboardTemplate`: cards resumen + acciones rapidas + secciones verticales.

## Reglas

- Template nunca contiene logica de negocio.
- Template recibe slots/composables hijos y estado UI necesario.
- Todos los templates consumen tokens y componentes base.

## Beneficios esperados

- Consistencia visual y de comportamiento.
- Menor costo de nuevas features.
- Menos regresiones de spacing/insets/jerarquia.
