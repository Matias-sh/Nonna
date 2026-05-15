# 04 - Product Components

## Objetivo

Estandarizar componentes compuestos especificos de Nonna usando foundation + componentes base.

## Principios

- No duplicar logica visual ya resuelta en base components.
- No introducir tokens locales fuera de foundation.
- API centrada en dominio de producto, no en detalles de estilo.

## Ejemplos de componentes de producto

- Cards de cofre y memoria.
- Card de notificación (`NotificationItemCard`) con estado leído/no leído.
- Headers de seccion del producto.
- Bloques de estado vacio de cada feature.
- Controles de filtros propios del dominio.
- Modales de invitaciones y acciones contextuales.

## Contrato

- Input de datos via modelo UI estable.
- Eventos de usuario por callbacks explicitos.
- Superficie visual consistente con tema global.
- Previews con dataset realista y casos edge.

## Estrategia de migracion

- Migrar feature por feature.
- Evitar cambiar comportamiento de negocio durante migracion visual.
- Documentar breaking changes de API en changelog.

## Implementado en notificaciones

- `NotificationItemCard`:
  - Responsabilidad: render de notificación individual con badge, metadata y acción marcar leída.
  - Reutiliza: `NonnaCard`, `NonnaButton`, tokens (`NonnaSpacing`, `NonnaCorners`).
  - Estado: leído/no leído + `markingAsRead`.
  - Sin lógica de negocio ni acceso a repositorios.
