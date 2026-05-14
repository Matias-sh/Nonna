# 06 - Accessibility

## Objetivo

Establecer accesibilidad como requerimiento de salida, no como mejora opcional.

## Checklist de auditoria

- `contentDescription` en iconos informativos.
- Etiquetas y ayuda contextual en inputs.
- Touch target minimo 48dp.
- Contraste de color adecuado para texto y acciones.
- Orden de foco coherente.
- Semantics para componentes custom.
- Soporte para TalkBack en flujos criticos.

## Hallazgos detectados (estado actual)

### Critico

- Ninguno en la pasada inicial.

### Alto

- Pantallas con botones/iconos de accion aun mezclan casos con `contentDescription = null` donde deberia describirse la accion.
  - Archivos afectados: `ui/screens/*` (casos puntuales en icon buttons).

### Medio

- Falta de estrategia uniforme de `semantics` para componentes custom fuera de inputs/chips.
  - Archivos afectados: `ui/components/*` (cards y bloques clickeables compuestos).

### Bajo

- Contraste y tamanos tactiles correctos en gran parte de componentes, pero sin checklist automatizado por PR.

## Propuesta de correccion

1. Auditar iconografia interactiva por feature y completar descripciones faltantes.
2. Estandarizar `semantics` en componentes base clickeables.
3. Agregar checklist obligatorio de accesibilidad en code review.
4. Incorporar tests de accesibilidad basicos en Compose UI tests.

## Reglas para futuros componentes

- Si un icono es decorativo, usar `contentDescription = null`.
- Si comunica accion/estado, describir intencion de forma clara.
- Validaciones de formulario deben ser audibles y visibles.
- No depender solo del color para comunicar estado.

## Definicion de done por pantalla

- Navegable por lector de pantalla.
- CTA principal identificable.
- Errores de validacion accesibles.
- Sin bloqueos de interaccion por targets pequenos.
